package za.co.inflationcalc.utils;

import android.text.SpannableStringBuilder;

/**
 * Created by Laurie on 9/14/2015.
 */
public class StringUtil {
    /**
     * Determines whether the string provided is null or empty
     *
     * @param text the text to check
     * @return True if the string provided is null or has no length; otherwise,
     * false;
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.length() == 0;
    }

    /**
     * Replaces the target string in the supplied text with the replacement string
     *
     * @param text   the text where in the replacement should occur
     * @param target the string to replace
     * @param with   the string to replace it with
     * @return a new string containing the replacement
     */
    public static CharSequence replace(CharSequence text, CharSequence target, CharSequence with) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        int pos = text.toString().indexOf(target.toString());
        if (pos >= 0) {
            ssb.replace(pos, pos + target.toString().length(), with != null ? with : "");
            return ssb;
        }

        return ssb;
    }

    /**
     * Returns a formatted char sequence, using the supplied format and arguments,
     * localized to the given locale. The only allowed format is $s indicating a string
     * that should be replaced
     *
     * @param text       the text containing the format markers
     * @param formatArgs the string to replace the formats
     * @return a new string containing the replacement
     */
    public static CharSequence format(CharSequence text, CharSequence... formatArgs) {
        final String MARKER = "$s";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        int pos = 0;
        for (CharSequence arg : formatArgs) {
            pos = ssb.toString().indexOf(MARKER, pos);
            if (pos == -1) {
                break;
            }
            ssb.replace(pos, pos + MARKER.length(), arg);
            pos += MARKER.length();
        }
        return ssb;
    }
}
