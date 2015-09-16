package za.co.inflationcalc.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Laurie on 9/16/2015.
 */
public class MathUtil {

    public static Double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Number of decimal places has to be set");
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
