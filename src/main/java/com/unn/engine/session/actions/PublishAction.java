package com.unn.engine.session.actions;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.DatasetDescriptor;
import com.unn.common.dataset.Feature;
import com.unn.common.dataset.Header;
import com.unn.common.server.NetworkUtils;
import com.unn.common.server.StandardResponse;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.server.services.MaestroService;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Utils;
import com.unn.engine.Config;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.datacenter.DatacenterLocator;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.metadata.ValueMapper;
import com.unn.common.mining.MiningReport;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.prediction.Prediction;
import com.unn.engine.session.Session;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

public class PublishAction extends Action {
    Session session;
    int upstreamLayer;
    private DatasetLocator datasetLocator;

    public PublishAction() { }

    public void setSession(Session session) {
        this.session = session;
    }

    public PublishAction withSession(Session session) {
        setSession(session);
        return this;
    }

    public Session getSession() {
        return session;
    }

    public int getUpstreamLayer() {
        return upstreamLayer;
    }

    public void setUpstreamLayer(int upstreamLayer) {
        this.upstreamLayer = upstreamLayer;
    }

    public void setDatasetLocator(DatasetLocator datasetLocator) {
        this.datasetLocator = datasetLocator;
    }

    public DatasetLocator getDatasetLocator() {
        return datasetLocator;
    }

    public void act() {
        this.sendMiningReport();
        this.fetchPredictPublish();
        System.out.println("|PublisherActor| Fetch -> Predict -> Publish done.");
    }

    private void sendMiningReport() {
        try {
            MiningReport report = this.session.getReport();
            MaestroService service = Utils.getMaestro();
            Call<StandardResponse> call = service.sendMiningReport(report);
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchPredictPublish() {
        DatasetDescriptor descriptor = this.register(this);
        for(;;) {
            Dataset dataset = fetchUnpredicted(descriptor);
            if (dataset == null) {
                break;
            }
            OuterDataset outerDataset = Datasets.toOuterDataset(dataset);
            System.out.println(String.format("|PublishAction| Batch predicting %d rows", outerDataset.sampleCount()));
            if (outerDataset.sampleCount() == 0) {
                break;
            }
            HashMap<String, ArrayList<Prediction>> predictions = batchPredict(outerDataset);
            publishPredictions(descriptor, predictions);
        }
    }

    private String[] getHeader(String csv) {
        return csv.split("\n")[0].split(",");
    }

    private Dataset fetchUnpredicted(DatasetDescriptor descriptor) {
        // NOTE: fetch source rows that are predictable and have no prediction yet
        for (int retry = 0; retry < 10; ++retry) {
            try {
                DatacenterService service = Utils.getDatacenter();
                Call<String> csv = service.fetchUnpredicted(descriptor.getNamespace());
                Response<String> response = csv.execute();
                String body = response.body();
                if (body == null) {
                    return null;
                }
                DatasetDescriptor upstreamDescriptor = new DatasetDescriptor()
                        .withHeader(new Header().withNames(getHeader(body)));
                Dataset dataset = new CSVHelper().parse(body)
                        .withDescriptor(upstreamDescriptor);
                return dataset;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private HashMap<String, ArrayList<Prediction>> batchPredict(OuterDataset dataset) {
        // NOTE: receives a OuterDataset, converts to InnerDataset and makes predictions
        HashMap<String, ArrayList<Prediction>> predictions = new HashMap<>();
        for (Map.Entry<String, MiningScope> entry : this.getSession().getScopes().entrySet()) {
            ArrayList<Prediction> refPredictions = new ArrayList<>();
            MiningScope scope = entry.getValue();
            ValueMapper mapper = scope.getMapper();
            InnerDataset innerDataset = Datasets.toInnerDataset(dataset, mapper);
            for (Integer time : innerDataset.getTimes()) {
                HashMap<IFeature, Integer> input = innerDataset.bundleSample(time);
                Double prediction = scope.predict(input);
                refPredictions.add(new Prediction()
                        .withTime(time)
                        .withValue(prediction));
            }
            predictions.put(entry.getKey(), refPredictions);
        }
        return predictions;
    }

    private String[] bundleDependencies(DatacenterLocator locator) {
        ArrayList<String> dependencies = new ArrayList<>();
        HashMap<String, List<String>> options = locator.getOptions();
        for (Map.Entry<String, List<String>> entry : options.entrySet()) {
            String namespace = entry.getKey();
            List<String> features = entry.getValue();
            for (String feature : features) {
                Feature f = new Feature()
                        .withNamespace(namespace)
                        .withColumn(feature);
                dependencies.add(f.toString());
            }
        }
        return dependencies.toArray(new String[dependencies.size()]);
    }

    private DatasetDescriptor register(PublishAction action) {
        String id = this.session.getRole().getAgent().getUuid()
                .replace("-", "")
                .substring(0, 5);
        String namespace = String.format("com.unn.engine.%s", id);
        DatacenterLocator locator = (DatacenterLocator) action.getDatasetLocator();
        String[] dependencies = bundleDependencies(locator);
        ArrayList<String> refs = new ArrayList<>();
        refs.add(Config.get().PRIMER);
        for (String scopeId : action.getSession().getScopes().keySet()) {
            refs.add(scopeId);
        }
        String[] names = refs.toArray(new String[refs.size()]);
        DatasetDescriptor descriptor = new DatasetDescriptor()
            .withLayer(action.getUpstreamLayer()+1)
            .withHeader(new Header().withNames(names))
            .withNamespace(namespace)
            .withUpstreamDependencies(dependencies)
            .withDescription("Mined dataset")
            .withMakerPrimers(this.session.getMakerTimes());
        NetworkUtils.registerAgent(descriptor);
        return descriptor;
    }

    private void publishPredictions(DatasetDescriptor descriptor, HashMap<String, ArrayList<Prediction>> predictions) {
        // NOTE: receives an array of predictions, converts it to a Dataset and publishes it
        Dataset dataset = Datasets.toDataset(descriptor, predictions);
        NetworkUtils.uploadDataset(dataset);
    }
}
