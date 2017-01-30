package za.co.inflationcalc.model;

import android.text.TextUtils;

import org.parceler.Parcel;

/**
 * POJO describing a start-date object
 * <p/>
 * Created by Laurie on 9/15/2015.
 */
@SuppressWarnings("WeakerAccess")
@Parcel
public class StartDate {

    String year;
    String month;
    String day;

    public String apiRepresentation;

    public StartDate() {
        // Default empty constructor required by Parceler
    }

    public StartDate(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;

        convertToApiFormat();
    }

    private void convertToApiFormat() {
        apiRepresentation = year + "-" + month + "-" + day;
    }

    public String getApiRepresentation() {
        if (TextUtils.isEmpty(apiRepresentation)) {
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
