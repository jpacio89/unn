package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.metadata.DiscreteValuesDescriptor;
import com.unn.engine.metadata.NumericValuesDescriptor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TestValueMapper {
    String[] getNumericParts(String suffix, String group) {
        return group
            .replace("numeric_", "")
            .replace(suffix, "")
            .split("_");
    }

    @Test
    public void testNumericDescriptor() {
        OuterDataset outer = Datasets.dummy1();
        ValueMapper mapper = new ValueMapper(outer);

        assertEquals(mapper.getFeatures().size(), 5);
        assertFalse(mapper.getFeatures().contains("id"));
        assertTrue(mapper.getFeatures().contains("primer"));
        assertTrue(mapper.getFeatures().contains("x"));
        assertTrue(mapper.getFeatures().contains("y"));
        assertTrue(mapper.getFeatures().contains("z"));
        assertTrue(mapper.getFeatures().contains("reward"));

        mapper.analyzeValues("x");
        ValuesDescriptor descriptor = mapper.getValuesDescriptorByFeature("x");

        assertNotNull(descriptor);
        assertTrue(descriptor instanceof NumericValuesDescriptor);

        NumericValuesDescriptor numericDescriptor = (NumericValuesDescriptor) descriptor;
        String suffix = String.format("_%s", numericDescriptor.getSuffix());

        assertEquals(numericDescriptor.groupCount, Config.DEFAULT_NUMERIC_CLUSTER_COUNT);
        numericDescriptor.getGroups().forEach((group -> {
            assertTrue(group.startsWith("numeric_"));
            assertTrue(group.endsWith(suffix));
            String[] parts = getNumericParts(suffix, group);
            Double threshold = Double.parseDouble(parts[0]);
            assertTrue(numericDescriptor.possibleValues.contains(threshold));
        }));

        for (int i = 0; i < 100; ++i) {
            double guess = Math.random();
            ArrayList<String> groups = numericDescriptor.getGroupByOuterValue(Double.toString(guess));
            groups.forEach(group -> {
                String[] parts = getNumericParts(suffix, group);
                Double midPoint = Double.parseDouble(parts[0]);
                Boolean inverted = Boolean.parseBoolean(parts[1]);
                assertTrue(guess >= midPoint ? !inverted : inverted);
            });
            numericDescriptor.getGroups().stream()
                .filter(e -> !groups.contains(e))
                .forEach(group -> {
                    String[] parts = getNumericParts(suffix, group);
                    Double midPoint = Double.parseDouble(parts[0]);
                    Boolean inverted = Boolean.parseBoolean(parts[1]);
                    assertFalse(guess >= midPoint ? !inverted : inverted);
                });
        }

        // TODO: test functor getter?
    }
}
