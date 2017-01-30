package za.co.inflationcalc.model;

import org.parceler.Parcel;

/**
 * POJO describing a result object
 * <p/>
 * Created by Laurie on 9/14/2015.
 */
@SuppressWarnings("WeakerAccess")
@Parcel
public class Result {

    private double currentValue;
    private double reverseValue;

    public Result() {
        // Default empty constructor required by Parceler
    }

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

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public void setReverseValue(double reverseValue) {
        this.reverseValue = reverseValue;
    }
}

