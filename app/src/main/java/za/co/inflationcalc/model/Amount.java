package za.co.inflationcalc.model;

/**
 * Created by Laurie on 9/14/2015.
 */
public class Amount {
    private double amountValue;

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
