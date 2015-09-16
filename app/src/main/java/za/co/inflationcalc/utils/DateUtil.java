package za.co.inflationcalc.utils;

/**
 * Created by Laurie on 9/15/2015.
 */
public class DateUtil {
    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    public static boolean isDateInputLessThan10(int input) {
        if (input < 10) {
            return true;
        }

        return false;
    }

    public static String convertToPrecedingZero(int input) {
        if (isDateInputLessThan10(input) && input > 0) {
            return "0" + input;
        }

        return String.valueOf(input);
    }
}
