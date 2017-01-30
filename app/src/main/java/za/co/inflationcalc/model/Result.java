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

    public String currentValue;
    public String reverseValue;

    public Result() {
        // Default empty constructor required by Parceler
    }

    public Result(String currentValue, String reverseValue) {
        this.currentValue = currentValue;
        this.reverseValue = reverseValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getReverseValue() {
        return reverseValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public void setReverseValue(String reverseValue) {
        this.reverseValue = reverseValue;
    }
}

