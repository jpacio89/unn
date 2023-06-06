package com.unn.engine.skipconditionals;

public class SkipConditional {
    double skip;
    double threshold;
    int jumpOnFalse;
    int jumpOnTrue;

    public SkipConditional(double skip, double threshold, int jumpOnFalse, int jumpOnTrue) {
        this.skip = skip;
        this.threshold = threshold;
        this.jumpOnFalse = jumpOnFalse;
        this.jumpOnTrue = jumpOnTrue;
    }

    public double getSkip() {
        return skip;
    }

    public void setSkip(double skip) {
        this.skip = skip;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getJumpOnFalse() {
        return jumpOnFalse;
    }

    public void setJumpOnFalse(int jumpOnFalse) {
        this.jumpOnFalse = jumpOnFalse;
    }

    public int getJumpOnTrue() {
        return jumpOnTrue;
    }

    public void setJumpOnTrue(int jumpOnTrue) {
        this.jumpOnTrue = jumpOnTrue;
    }
}
