package za.co.inflationcalc.model;

import org.parceler.Parcel;

import za.co.inflationcalc.utils.StringUtil;

/**
 * Created by Laurie on 9/15/2015.
 */
@Parcel
public class StartDate {

    protected String year;
    protected String month;
    protected String day;

    private String apiRepresentation;

    public StartDate() {
        // Default empty constructor required by Parceler
    }

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

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
