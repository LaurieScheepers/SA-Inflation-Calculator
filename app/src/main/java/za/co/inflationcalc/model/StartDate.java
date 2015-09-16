package za.co.inflationcalc.model;

import za.co.inflationcalc.utils.StringUtil;

/**
 * Created by Laurie on 9/15/2015.
 */
public class StartDate {

    protected String year;
    protected String month;
    protected String day;

    private String apiRepresentation;

    public StartDate(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;

        convertToApiFormat();
    }

    public void convertToApiFormat() {
        apiRepresentation = year + "-" + month + "-" + day;
    }

    public String getApiRepresentation() {
        if (StringUtil.isNullOrEmpty(apiRepresentation)) {
            convertToApiFormat();
        }

        return apiRepresentation;
    }
}
