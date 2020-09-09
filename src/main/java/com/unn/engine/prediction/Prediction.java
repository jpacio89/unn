package com.unn.engine.prediction;

public class Prediction {
    Integer time;
    Double value;

    public Prediction() { }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Prediction withValue(Double value) {
        this.value = value;
        return this;
    }

    public Prediction withTime(Integer time) {
        this.time = time;
        return this;
    }
}
