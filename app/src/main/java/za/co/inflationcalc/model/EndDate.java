package za.co.inflationcalc.model;

import za.co.inflationcalc.utils.LogUtil;

/**
 * Created by Laurie on 9/15/2015.
 */
public class EndDate extends StartDate {

    public EndDate(String year, String month, String day) {
        super(year, month, day);

        printEndDate();
    }

    private void printEndDate() {
        LogUtil.d("Constructing end date object: " + "year = " + year + ", month = " + month + ", day = " + day);
    }
}
