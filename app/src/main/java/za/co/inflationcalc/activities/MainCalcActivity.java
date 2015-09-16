package za.co.inflationcalc.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.codetroopers.betterpickers.datepicker.DatePickerDialogFragment;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import za.co.inflationcalc.R;
import za.co.inflationcalc.comms.http.RestClient;
import za.co.inflationcalc.model.Amount;
import za.co.inflationcalc.model.EndDate;
import za.co.inflationcalc.model.StartDate;
import za.co.inflationcalc.utils.DateUtil;
import za.co.inflationcalc.utils.LogUtil;
import za.co.inflationcalc.utils.MathUtil;

public class MainCalcActivity extends AppCompatActivity {

    public static final int START_DATE_EDIT_REF = 0;
    public static final int END_DATE_EDIT_REF = 1;
    public static final int AMOUNT_EDIT_REF = 2;

    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText amountEditText;
    private EditText resultEditText;

    private DatePickerBuilder startDatePicker;
    private DatePickerBuilder endDatePicker;

    private NumberPickerBuilder amountPicker;

    private Calendar startCalendarDate;
    private Calendar endCalendarDate;

    private Button calculateButton;

    private StartDate startDate;
    private EndDate endDate;

    private int currentYear;
    private int currentMonth;
    private int currentDay;

    private int endYear;
    private int endMonth;
    private int endDay;

    private int startYear;
    private int startMonth;
    private int startDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_calc_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_calc_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setLogo(R.drawable.sa_round_icon_logo);

        // Get current date and automatically set as the end date
        endCalendarDate = Calendar.getInstance(TimeZone.getDefault());
        startCalendarDate = Calendar.getInstance(TimeZone.getDefault());

        currentYear = endYear = endCalendarDate.get(Calendar.YEAR);
        LogUtil.d("END_YEAR: " + endYear);

        currentMonth = endMonth = endCalendarDate.get(Calendar.MONTH) + 1; // Jan starts at 0
        LogUtil.d("END_MONTH: " + endMonth);

        currentDay = endDay = endCalendarDate.get(Calendar.DAY_OF_MONTH);
        LogUtil.d("END_DAY: " + endDay);

        startDateEditText = (EditText) findViewById(R.id.enter_start_date_edit_text);
        endDateEditText = (EditText) findViewById(R.id.enter_end_date_edit_text);
        amountEditText = (EditText) findViewById(R.id.enter_amount_edit_text);
        resultEditText = (EditText) findViewById(R.id.result_edit_text);

        startDatePicker = new DatePickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        endDatePicker = new DatePickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        amountPicker = new NumberPickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setReference(AMOUNT_EDIT_REF)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        amountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    amountPicker.show();
                    return true;
                }
                return false;
            }
        });

        amountPicker.addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandler() {
            @Override
            public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
                if (reference == AMOUNT_EDIT_REF) {
                    String numberString = decimal > 0 ? number + "." + decimal : String.valueOf(number);

                    if (amountEditText.getText().length() == 0) {
                        amountEditText.setText("R");
                    }

                    if (amountEditText.getText().length() > 0 && !amountEditText.getText().toString().contains("R")) {
                        amountEditText.setText("R");
                    }

                    amountEditText.append(numberString);
                    amountEditText.setSelection(amountEditText.getText().length());
                }
            }
        });

        String day = DateUtil.convertToPrecedingZero(endDay);
        String month = DateUtil.convertToPrecedingZero(endMonth);
        String year = String.valueOf(endYear);

        endDateEditText.setText(day + "/" + month + "/" + year);

        endDate = new EndDate(year, month, day);

        startDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    startDatePicker.setReference(START_DATE_EDIT_REF);

                    if (startDay > 0 && startMonth >= 0 && startYear > 0) {
                        startDatePicker.setDayOfMonth(startDay);
                        startDatePicker.setMonthOfYear(startMonth);
                        startDatePicker.setYear(startYear);
                    }

                    startDatePicker.show();
                    return true;
                }
                return false;
            }
        });

        endDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endDatePicker.setReference(END_DATE_EDIT_REF);

                    if (endDay > 0 && endMonth > 0 && endYear > 0) {
                        endDatePicker.setDayOfMonth(endDay);
                        endDatePicker.setMonthOfYear(endMonth - 1);
                        endDatePicker.setYear(endYear);
                    }

                    endDatePicker.show();
                }

                return true;
            }
        });

        startDatePicker.addDatePickerDialogHandler(new DatePickerDialogFragment.DatePickerDialogHandler() {
            @Override
            public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
                int realStartMonthOfYear = monthOfYear + 1;

                LogUtil.d("START_DATE: Reference= " + (reference == START_DATE_EDIT_REF ? "START_DATE_PICKER" : "") + ", year = " + year + ", converted month = " + realStartMonthOfYear + ", day = " + dayOfMonth);

                startYear = year;
                startMonth = realStartMonthOfYear;
                startDay = dayOfMonth;

                startCalendarDate.set(Calendar.YEAR, year);
                startCalendarDate.set(Calendar.MONTH, monthOfYear);
                startCalendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (endDateEditText.getText().length() > 0) {
                    if (endCalendarDate != null && startCalendarDate.getTimeInMillis() > endCalendarDate.getTimeInMillis()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainCalcActivity.this, "Start date obviously can't be after the future date." +
                                        "\n\nCmon man, get yourself together :)", Toast.LENGTH_LONG).show();
                                startDatePicker.setYear(0);
                                startDatePicker.setMonthOfYear(0);
                                startDatePicker.setDayOfMonth(0);
                            }
                        });

                        return;
                    }
                }

                String day = DateUtil.convertToPrecedingZero(dayOfMonth);
                String month = DateUtil.convertToPrecedingZero(realStartMonthOfYear);
                String yearStringValue = String.valueOf(year);

                startDateEditText.setText(day + "/" + month + "/" + yearStringValue);

                startDate = new StartDate(yearStringValue, month, day);

                if (startDateEditText.getText().length() > 0 && endDateEditText.getText().length() > 0) {
                    calculateButton.setEnabled(true);
                }
            }
        });

        endDatePicker.addDatePickerDialogHandler(new DatePickerDialogFragment.DatePickerDialogHandler() {
            @Override
            public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
                int realEndMonthOfYear = monthOfYear + 1;
                LogUtil.d("END_DATE: Reference= " + (reference == END_DATE_EDIT_REF ? "END_DATE_PICKER" : "") + ", year = " + year + ", month = " + realEndMonthOfYear + ", day = " + dayOfMonth);

                endYear = year;
                endMonth = realEndMonthOfYear;
                endDay = dayOfMonth;

                endCalendarDate.set(Calendar.YEAR, year);
                endCalendarDate.set(Calendar.MONTH, monthOfYear);
                endCalendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (startDateEditText.getText().length() > 0) {
                    if (startCalendarDate != null && endCalendarDate.getTimeInMillis() < startCalendarDate.getTimeInMillis()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainCalcActivity.this, "Let's not go back into the past. You'll get the reverse answer anyway" +
                                        "\n\nNow you're just being difficult :)", Toast.LENGTH_LONG).show();
                                endDatePicker.setYear(currentYear);
                                endDatePicker.setMonthOfYear(currentMonth);
                                endDatePicker.setDayOfMonth(currentDay);
                            }
                        });

                        return;
                    }
                }

                String day = DateUtil.convertToPrecedingZero(dayOfMonth);
                String month = DateUtil.convertToPrecedingZero(realEndMonthOfYear);
                String yearStringValue = String.valueOf(year);

                endDateEditText.setText(day + "/" + month + "/" + yearStringValue);

                endDate = new EndDate(yearStringValue, month, day);

                if (startDateEditText.getText().length() > 0 && endDateEditText.getText().length() > 0) {
                    calculateButton.setEnabled(true);
                }
            }
        });

        calculateButton = (Button) findViewById(R.id.calculate_btn);
        calculateButton.setEnabled(false);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountEditText.getText().length() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainCalcActivity.this, "Please enter an amount to make the calculation :)", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    String realValue = amountEditText.getText().toString().contains("R") ?
                            amountEditText.getText().toString().replace("R", "") : amountEditText.getText().toString();

                    Amount amountObj = new Amount(Double.parseDouble(realValue));

                    if (startDate != null && endDate != null) {
                        RequestParams params = new RequestParams();
                        params.put("date1", startDate.getApiRepresentation());
                        params.put("date2", endDate.getApiRepresentation());
                        params.put("amount", amountObj.getAmountValue());

                        LogUtil.d("Input params:" + params.toString());

                        RestClient.get(MainCalcActivity.this, RestClient.BASE_URL, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                                try {
                                    LogUtil.d("Yay, it worked!!!");
                                    LogUtil.d("Result: " + response.toString());

                                    double answer = MathUtil.round(response.optDouble("Answer"), 2);

                                    resultEditText.setText("R" + String.valueOf(answer));

                                } catch (Exception e) {
                                    LogUtil.e("Error in creating video thumbnail view", e);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                LogUtil.e("Inflationcalc request failed. Status code = " + statusCode, throwable);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                LogUtil.e("Inflationcalc Request failed. Status code = " + statusCode, throwable);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                LogUtil.e("Inflationcalc Request failed. Status code = " + statusCode, throwable);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_calc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
