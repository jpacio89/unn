package com.unn.engine.skipconditionals;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.common.mining.MiningReport;
import com.unn.common.operations.AgentRole;
import com.unn.engine.Config;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import com.unn.engine.utils.RandomManager;

import java.util.*;

public class SkipConditionalRunner {
    private int maxCount;
    int priorityMaxSize = 1000;
    PriorityQueue<SkipConditionalList> bestLists;

    Random randomizer = new Random();

    public SkipConditionalRunner init(int maxCount) {
        this.maxCount = maxCount;
        this.bestLists = new PriorityQueue<>(new ListComparator());
        return this;
    }

    public void run(int[][] data, String[] targets) {
        while(true) {
            // Generate SkipConditional list
            SkipConditionalList guess = getRandomList();

            // Given a SkipConditional list generate OuterDataset
            OuterDataset outerDataset = buildDatasetFromData(data, targets, guess);

            // Given a outerDataset mine it and produce a model
            Session session = mine(outerDataset, "class");

            // Given a model analyze it and push to memory if it is of interest
            analyzeResults(guess, session);
        }
    }

    private void analyzeResults(SkipConditionalList list, Session session) {
        MiningReport report = session.getReport();
        HashMap<String, ConfusionMatrix> confusionMatrixes = report.getConfusionMatrixes();

        if (report == null || report.getConfusionMatrixes().size() == 0) {
            return;
        }

        int maxAccuracy = 0;

        for (Map.Entry<String, ConfusionMatrix> entry : confusionMatrixes.entrySet()) {
            // String key = entry.getKey();
            ConfusionMatrix value = entry.getValue();
            if (value.getTpr() > maxAccuracy) {
                maxAccuracy = value.getTpr();
            }
        }

        list.setBestAccuracy(maxAccuracy);
        list.setSession(session);
        SkipConditionalList worstListSoFar = this.bestLists.peek();

        if (this.bestLists.size() >= priorityMaxSize) {
            if (worstListSoFar.getBestAccuracy() < maxAccuracy) {
                this.bestLists.poll();
                this.bestLists.add(list);
            }
        } else {
            this.bestLists.add(list);
        }

        Iterator<SkipConditionalList> listIt = this.bestLists.iterator();

        while (listIt.hasNext()) {
            SkipConditionalList itList = listIt.next();
            System.out.printf("List Accuracy: %d%%%n", itList.getBestAccuracy());
        }
    }

    private Session mine(OuterDataset outerDataset, String target) {
        Context context = new Context();
        AgentRole role = new AgentRole();

        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig(target, new ArrayList<>()));

        action.act();

        MiningReport report = action.getSession().getReport();
        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("Report statistics --> null");
        } else {
            System.out.println(String.format("Report statistics -->\n%s", report.toString()));
        }

        return session;
    }

    private OuterDataset buildDatasetFromData(int[][] data, String[] targets, SkipConditionalList list) {
        // TODO: fix this
        int dataValueRange = 256;
        int dataValueMin = 0;

        OuterDataset outerDataset = new OuterDataset();
        String[] features = new String[list.getList().size()+1];

        for (int i = 0; i < features.length; ++i) {
            features[i] = String.format("skipcond-%d", i);
        }

        features[list.getList().size()] = "class";
        outerDataset.setHeader(features);

        for (int i = 0; i < data.length; ++i) {
            int currentIndex = 0;
            SkipConditional sc = list.getList().get(currentIndex);
            int skip = (int) (sc.getSkip() * data[i].length);
            String[] memory = new String[list.getList().size()+1];
            for (int k = 0; k < memory.length; ++k) {
                memory[k] = "-1";
            }
            for (int j = skip; j < data[i].length; ++j) {
                if (data[i][j] > dataValueMin + sc.getThreshold() * dataValueRange) {
                    memory[currentIndex] = Integer.toString(data[i][j]);
                    currentIndex = sc.getJumpOnTrue();
                } else {
                    currentIndex = sc.getJumpOnFalse();
                }
                sc = list.getList().get(currentIndex);
                skip = (int) (sc.getSkip() * data[i].length);
                j += skip - 1;
            }

            memory[list.getList().size()] = targets[i];
            outerDataset.addSample(memory);
        }

        return outerDataset;
    }

    private SkipConditionalList getRandomList() {
        int listSize = RandomManager.rand(1, this.maxCount);
        SkipConditionalList list = new SkipConditionalList();
        for (int i = 0; i < listSize; ++i) {
            double skipGuess = this.randomizer.nextDouble();
            double thresholdGuess = this.randomizer.nextDouble();
            int jumpOnFalseIndex = RandomManager.rand(0, listSize - 1);
            int jumpOnTrueIndex = RandomManager.rand(0, listSize - 1);
            SkipConditional sc = new SkipConditional(skipGuess,thresholdGuess,
                    jumpOnFalseIndex,jumpOnTrueIndex);
            list.add(sc);
        }
        return list;
    }

    class ListComparator implements Comparator<SkipConditionalList> {
        public int compare(SkipConditionalList s1, SkipConditionalList s2) {
            if (s1.bestAccuracy < s2.bestAccuracy)
                return -1;
            else if (s1.bestAccuracy > s2.bestAccuracy)
                return 1;
            return 0;
        }
    }
}
