package com.unn.engine.dataset;

import com.unn.common.boosting.Archive;
import com.unn.common.boosting.BrainfuckInterpreter;
import com.unn.common.boosting.TuringConfig;
import com.unn.common.dataset.Row;
import com.unn.engine.functions.SimpleFeature;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.interfaces.IFeature;

import java.util.ArrayList;
import java.util.HashMap;

public class BoosterProvider {
    public final int BOOSTER_COUNT = 10;
    private HashMap<String, String> functorMapper;
    private ArrayList<IFeature> functors;

    public final TuringConfig turingConfig = new TuringConfig();
    InnerDataset seedDataset;

    public BoosterProvider(InnerDataset _seedDataset) {
        this.seedDataset = _seedDataset;
    }

    public InnerDataset boost(ArrayList<IFeature> targetGroups) {
        // Info: cloning original dataset and boost new one
        final InnerDataset boosted = this.seedDataset.copy();
        this.initFunctors(boosted);

        this.seedDataset.getTimes().stream().forEach((time) -> {
            // Info: bundle array of parameters to pass to BF interpreter
            String[] arguments = this.seedDataset.getFunctors().stream()
                .filter(f -> !targetGroups.contains(f))
                .map(f -> Integer.toString(this.seedDataset.getValueByTime(f, time)))
                .toArray(String[]::new);

            HashMap<String, Row> programOutput = new HashMap<>();

            this.functors.forEach(functor -> {
                // Info: get random program and apply it to dataset
                String functorName = functor.getName();
                int bufferIndex = Integer.parseInt(functorName.split("-")[2]);
                String program = this.functorMapper.get(functorName);

                if (!programOutput.containsKey(program)) {
                    // TODO: instead of running program, try to use results from cache
                    BrainfuckInterpreter bf = new BrainfuckInterpreter();
                    bf.interpret(program, arguments);
                    programOutput.put(program, bf.toRow(turingConfig.MAX_TVAR_COUNT));
                }

                String[] rowValues = programOutput.get(program).getValues();

                // Info: for each BF output, add to boosted InnerDataset
                boosted.add(new ValueTime(functor,
                    Integer.parseInt(rowValues[bufferIndex]), time));
            });
        });
        return boosted;
    }

    private void initFunctors(InnerDataset boosted) {
        ArrayList<IFeature> functors = this.seedDataset.getFunctors();
        this.functors = new ArrayList<>();
        this.functorMapper = new HashMap<>();

        for (int i = 0; i < BOOSTER_COUNT; ++i) {
            Archive.ArchiveRecord record = Archive.get().getRandomProgram();
            String programHash = Archive.get()
                .getHashByProgram(record.getProgram());

            for (int index : record.getValidFeatureIndexes()) {
                String functorName = String.format("booster-%s-%d", programHash, index);
                SimpleFeature function = new SimpleFeature();
                function.setName(functorName);
                this.functors.add(function);
                this.functorMapper.put(functorName, record.getProgram());
            }
        }

        ArrayList<IFeature> boostedFunctors = new ArrayList<>();
        boostedFunctors.addAll(functors);
        boostedFunctors.addAll(this.functors);
        boosted.setFunctors(boostedFunctors);
    }
}
