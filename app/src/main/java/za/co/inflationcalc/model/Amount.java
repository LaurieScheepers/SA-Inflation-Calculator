package za.co.inflationcalc.model;

import org.parceler.Parcel;

/**
 * POJO describing an amount object
 *
 * <p/>
 * Created by Laurie on 9/14/2015.
 */
@SuppressWarnings("WeakerAccess")
@Parcel
public class Amount {
    private double amountValue;

    public Amount() {
        // Default empty constructor required by Parceler
    }

    public Amount(double value) {
        this.amountValue = value;
    }

    public double getValue() {
        return amountValue;
    }

    public void setValue(double value) {
        this.amountValue = value;
    }
}
