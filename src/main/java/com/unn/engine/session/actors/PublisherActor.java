package com.unn.engine.session.actors;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.DatasetDescriptor;
import com.unn.common.dataset.Feature;
import com.unn.common.dataset.Header;
import com.unn.common.server.NetworkUtils;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Utils;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.datacenter.DatacenterLocator;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.prediction.Prediction;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PublishAction;
import retrofit2.Call;
import retrofit2.Response;

import javax.management.Descriptor;
import java.io.IOException;
import java.util.*;

public class PublisherActor extends Actor {
	PublishAction action;
	Thread thrd;

	public PublisherActor(PublishAction action) {
		this.action = action;
	}

	public ActionResult write() {
		if (this.thrd != null) {
			return null;
		}
		this.thrd = new Thread(() -> {
			fetchPredictPublish();
			this.thrd = null;
		});
		this.thrd.start();
		return null;
	}

	private void fetchPredictPublish() {
		for(;;) {
			DatasetDescriptor descriptor = this.register(this.action);
			Dataset dataset = fetchUnpredicted(descriptor);
			OuterDataset outerDataset = Datasets.toOuterDataset(dataset);
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
		try {
			DatacenterService service = Utils.getDatacenter(true);
			Call<String> csv = service.fetchUnpredicted(descriptor.getNamespace());
			Response<String> response = csv.execute();
			String body = response.body();
			DatasetDescriptor upstreamDescriptor = new DatasetDescriptor()
				.withHeader(new Header().withNames(getHeader(body)));
			Dataset dataset = new CSVHelper().parse(body)
				.withDescriptor(upstreamDescriptor);
			return dataset;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private HashMap<String, ArrayList<Prediction>> batchPredict(OuterDataset dataset) {
		// NOTE: receives a OuterDataset, converts to InnerDataset and makes predictions
		HashMap<String, ArrayList<Prediction>> predictions = new HashMap<>();
        for (Map.Entry<String, MiningScope> entry : action.getSession().getScopes().entrySet()) {
			ArrayList<Prediction> refPredictions = new ArrayList<>();
			MiningScope scope = entry.getValue();
			ValueMapper mapper = scope.getMapper();
			JobConfig job = scope.getConfig();
			InnerDataset innerDataset = Datasets.toInnerDataset(dataset, mapper, job);
			for (Integer time : innerDataset.getTimes()) {
				HashMap<IOperator, Integer> input = innerDataset.bundleSample(time);
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
		String id = UUID.randomUUID().toString()
			.replace("-", "")
			.substring(0, 5);
		String namespace = String.format("com.unn.engine.%s", id);
		DatacenterLocator locator = (DatacenterLocator) action.getDatasetLocator();
		String[] dependencies = bundleDependencies(locator);
		ArrayList<String> refs = new ArrayList<>();
		refs.add("primer");
		for (String scopeId : action.getSession().getScopes().keySet()) {
			refs.add(scopeId);
		}
		String[] names = refs.toArray(new String[refs.size()]);
		DatasetDescriptor descriptor = new DatasetDescriptor()
			.withLayer(action.getUpstreamLayer()+1)
			.withHeader(new Header().withNames(names))
			.withNamespace(namespace)
			.withUpstreamDependencies(dependencies)
			.withDescription("Mined dataset");
		NetworkUtils.registerAgent(descriptor);
		return descriptor;
	}

	private void publishPredictions(DatasetDescriptor descriptor, HashMap<String, ArrayList<Prediction>> predictions) {
		// NOTE: receives an array of predictions, converts it to a Dataset and publishes it
		Dataset dataset = Datasets.toDataset(descriptor, predictions);
		NetworkUtils.uploadDataset(dataset);
	}
}
