package za.co.inflationcalc.model;

/**
 * Created by Laurie on 9/14/2015.
 */
public class Result {
    private double currentValue;
    private double reverseValue;

    public Result(double currentValue, double reverseValue) {
        this.currentValue = currentValue;
        this.reverseValue = reverseValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public double getReverseValue() {
        return reverseValue;
    }
}

