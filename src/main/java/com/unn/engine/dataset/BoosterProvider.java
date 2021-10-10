package com.unn.engine.dataset;

import com.unn.common.boosting.BrainfuckInterpreter;
import com.unn.common.boosting.TuringConfig;
import com.unn.common.dataset.Row;
import com.unn.engine.functions.Raw;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.interfaces.IFunctor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoosterProvider {
    public final int BOOSTER_COUNT = 10;

    public final TuringConfig turingConfig = new TuringConfig();
    InnerDataset seedDataset;

    public BoosterProvider(InnerDataset _seedDataset) {
        this.seedDataset = _seedDataset;
    }

    public InnerDataset boost(ArrayList<IFunctor> targetGroups) {
        InnerDataset boosted = new InnerDataset();
        boosted.copy(this.seedDataset);
        this.initFunctors();

        this.seedDataset.getTimes().stream().forEach((time) -> {
            // Info: bundle array of parameters to pass to BF interpreter
            String[] arguments = this.seedDataset.getFunctors().stream()
                .filter(f -> !targetGroups.contains(f))
                .map(f -> Integer.toString(this.seedDataset.getValueByTime(f, time)))
                .toArray(String[]::new);

            // TODO: for each program
            String program = "";
            BrainfuckInterpreter bf = new BrainfuckInterpreter();
            bf.interpret(program, arguments);
            Row row = bf.toRow(turingConfig.MAX_TVAR_COUNT);

            // Info: for each BF output, add to boosted InnerDataset
            IntStream.range(0, row.getValues().length)
                .forEach(index -> {
                    // TODO: add booster functor
                    boosted.add(new ValueTime(null,
                        Integer.parseInt(row.getValues()[index]), time));
                });
        });
        
        return boosted;
    }

    private void initFunctors() {
        ArrayList<IFunctor> functors = this.seedDataset.getFunctors();
        ArrayList<IFunctor> boostedFunctors = new ArrayList<>();
        boostedFunctors.addAll(functors);

        for (int i = 0; i < BOOSTER_COUNT; ++i) {
            // TODO: set FunctionDescriptor
            boostedFunctors.add(new Raw());
        }
    }
}
