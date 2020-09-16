package com.unn.engine.session.actors;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.DatasetDescriptor;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Utils;
import com.unn.engine.dataset.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.prediction.Prediction;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PublishAction;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
			Dataset dataset = fetchUnpredicted();
			OuterDataset outerDataset = Datasets.toOuterDataset(dataset);
			if (outerDataset.sampleCount() == 0) {
				break;
			}
			HashMap<String, ArrayList<Prediction>> predictions = batchPredict(outerDataset);
			publishPredictions(dataset.getDescriptor(), predictions);
		}
	}

	private Dataset fetchUnpredicted() {
		// NOTE: fetch source rows that are predictable and have no prediction yet
		try {
			DatacenterService service = Utils.getDatacenter(true);
			// TODO: send proper namespace
			Call<Dataset> csv = service.fetchUnpredicted("com.example.namespace");
			Response<Dataset> response = csv.execute();
			Dataset dataset = response.body();
			return dataset;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private HashMap<String, ArrayList<Prediction>> batchPredict(OuterDataset dataset) {
		// NOTE: receives a OuterDataset, converts to InnerDataset and makes predictions
		JobConfig job = action.getSession().getMineConfig();
		HashMap<String, ArrayList<Prediction>> predictions = new HashMap<>();
		// ArrayList<Prediction> predictions = new ArrayList<>();
        for (Map.Entry<String, MiningScope> entry : action.getSession().getScopes().entrySet()) {
			ArrayList<Prediction> refPredictions = new ArrayList<>();
			MiningScope scope = entry.getValue();
			ValueMapper mapper = scope.getMapper();
			InnerDataset innerDataset = Datasets.toInnerDataset(dataset, mapper, job);
			for (Integer time : innerDataset.getTimes()) {
				HashMap<IOperator, Integer> input = innerDataset.bundleSample(time);
				Double prediction = scope.predict(input);
				refPredictions.add(new Prediction()
					.withTime(time)
					.withValue(prediction));
			}
			predictions.put(scope.getRef(), refPredictions);
		}
		return predictions;
	}

	private void publishPredictions(DatasetDescriptor upstreamDescriptor, HashMap<String, ArrayList<Prediction>> predictions) {
		// NOTE: receives an array of predictions, converts it to a Dataset and publishes it
		Dataset dataset = Datasets.toDataset(upstreamDescriptor, predictions);
		String csv = new CSVHelper().toString(dataset);
		// TODO: send csv go Datacenter service
	}

	
}
