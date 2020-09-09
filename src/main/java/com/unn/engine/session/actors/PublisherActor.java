package com.unn.engine.session.actors;

import com.unn.common.dataset.Dataset;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Utils;
import com.unn.engine.dataset.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.mining.Model;
import com.unn.engine.prediction.Prediction;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PublishAction;
import com.unn.engine.session.actions.QueryAction;
import org.apache.commons.csv.CSVParser;

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
			OuterDataset dataset = fetchUnpredicted();
			if (dataset.sampleCount() == 0) {
				break;
			}
			ArrayList<Prediction> predictions = batchPredict(dataset);
			publishPredictions(predictions);
		}
	}

	private OuterDataset fetchUnpredicted() {
		// TODO: fetch source rows that are predictable and have no prediction yet
		DatacenterService service = Utils.getDatacenter(true);
		String csv = service.fetchUnpredicted("com.example.namespace");
		Dataset dataset = new CSVHelper().parse(csv);
		OuterDataset outerDataset = Datasets.toOuterDataset(dataset);
		return outerDataset;
	}

	private ArrayList<Prediction> batchPredict(OuterDataset dataset) {
		// NOTE: receives a OuterDataset, converts to InnerDataset and makes predictions
		ArrayList<Prediction> predictions = new ArrayList<>();
        for (Map.Entry<String, MiningScope> entry : action.getSession().getScopes().entrySet()) {
			MiningScope scope = entry.getValue();
			ValueMapper mapper = scope.getMapper();
			InnerDataset innerDataset = Datasets.toInnerDataset(dataset, mapper);
			for (Integer time : innerDataset.getTimes()) {
				// TODO: replace nulls
				Double prediction = scope.predict(null, null);
				predictions.add(new Prediction()
					.withTime(time)
					.withValue(prediction));
			}
		}
		return predictions;
	}

	private void publishPredictions(ArrayList<Prediction> predictions) {
		// NOTE: receives an array of predictions, converts it to a Dataset and publishes it
		Dataset dataset = new Dataset();
		// TODO: convert predictions to Dataset
		String csv = new CSVHelper().toString(dataset);
		// TODO: send csv go Datacenter service
	}

	
}
