package za.co.inflationcalc.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Laurie on 9/18/2015.
 */
public class KeyboardUtil {

    /**
     * Hides the keyboard associated with a view
     * @param context the context in which the view was created and shown
     * @param field the view that can take input
     */
    public static void hideKeyboard(Context context, View field) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(field.getWindowToken(), 0);
    }
}
