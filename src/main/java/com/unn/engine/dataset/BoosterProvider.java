package com.unn.engine.dataset;

import com.unn.common.boosting.Archive;
import com.unn.common.boosting.BrainfuckInterpreter;
import com.unn.common.boosting.TuringConfig;
import com.unn.common.dataset.Row;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.interfaces.IFunctor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

public class BoosterProvider {
    public final int BOOSTER_COUNT = 10;
    private HashMap<String, String> functorMapper;
    private ArrayList<IFunctor> functors;

    public final TuringConfig turingConfig = new TuringConfig();
    InnerDataset seedDataset;

    public BoosterProvider(InnerDataset _seedDataset) {
        this.seedDataset = _seedDataset;
    }

    public InnerDataset boost(ArrayList<IFunctor> targetGroups) {
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
                String functorName = functor.getDescriptor().getVtrName();
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
        ArrayList<IFunctor> functors = this.seedDataset.getFunctors();
        this.functors = new ArrayList<>();
        this.functorMapper = new HashMap<>();

        for (int i = 0; i < BOOSTER_COUNT; ++i) {
            Archive.ArchiveRecord record = Archive.get().getRandomProgram();
            String programHash = Archive.get()
                .getHashByProgram(record.getProgram());

            for (int index : record.getValidFeatureIndexes()) {
                String functorName = String.format("booster-%s-%d", programHash, index);
                Raw function = new Raw();
                function.setDescriptor(new FunctionDescriptor(functorName));
                this.functors.add(function);
                this.functorMapper.put(functorName, record.getProgram());
            }
        }

        ArrayList<IFunctor> boostedFunctors = new ArrayList<>();
        boostedFunctors.addAll(functors);
        boostedFunctors.addAll(this.functors);
        boosted.setFunctors(boostedFunctors);
    }
}
