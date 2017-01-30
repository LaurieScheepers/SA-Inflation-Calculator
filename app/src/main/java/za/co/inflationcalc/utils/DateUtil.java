package za.co.inflationcalc.utils;

/**
 * Contains some helper methods related to dates
 * <p/>
 * Created by Laurie on 9/15/2015.
 */
public class DateUtil {

    private static boolean isDateInputLessThan10(int input) {
        return input < 10;
    }

    public static String convertToPrecedingZero(int input) {
        if (isDateInputLessThan10(input) && input > 0) {
            return "0" + input;
        }

        return String.valueOf(input);
    }
}
