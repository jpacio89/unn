package com.unn.engine.mining;

import com.unn.engine.interfaces.IOperator;

public class ScopeConfig {
    IOperator featureSelector;

    public ScopeConfig(IOperator featureSelector) {
        this.featureSelector = featureSelector;
    }

    public IOperator getSelector() {
        return featureSelector;
    }

    public void setSelector(IOperator selector) {
        this.featureSelector = selector;
    }
}
