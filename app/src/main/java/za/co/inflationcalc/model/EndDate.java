package za.co.inflationcalc.model;

import org.parceler.Parcel;

import za.co.inflationcalc.utils.LogUtil;

/**
 * POJO describing an end-date object
 * <p/>
 * Created by Laurie on 9/15/2015.
 */
@SuppressWarnings("WeakerAccess")
@Parcel
public class EndDate extends StartDate {

    public EndDate() {
        // Default empty constructor required by Parceler
    }

    public EndDate(String year, String month, String day) {
        super(year, month, day);

        printEndDate();
    }

    private void printEndDate() {
        LogUtil.d("Constructing end date object: " + "year = " + year + ", month = " + month + ", day = " + day);
    }
}
