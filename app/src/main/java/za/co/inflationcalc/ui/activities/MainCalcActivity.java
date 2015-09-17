package za.co.inflationcalc.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.codetroopers.betterpickers.datepicker.DatePickerDialogFragment;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import za.co.inflationcalc.R;
import za.co.inflationcalc.comms.http.RestClient;
import za.co.inflationcalc.model.Amount;
import za.co.inflationcalc.model.EndDate;
import za.co.inflationcalc.model.Result;
import za.co.inflationcalc.model.StartDate;
import za.co.inflationcalc.utils.DateUtil;
import za.co.inflationcalc.utils.LogUtil;
import za.co.inflationcalc.utils.MathUtil;
import za.co.inflationcalc.utils.StringUtil;

/**
 * <p>
 * The main activity. This activity (the only one) controls most of the business logic
 * contained in the app,
 * </p>
 * Created by meyers on 2015-05-07.
 */
public class MainCalcActivity extends AppCompatActivity {

    public static final int START_DATE_EDIT_REF = 0;
    public static final int END_DATE_EDIT_REF = 1;

    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText amountEditText;
    private EditText resultEditText;

    private DatePickerBuilder startDatePicker;
    private DatePickerBuilder endDatePicker;

    private Calendar currentCalendarDate;
    private Calendar startCalendarDate;
    private Calendar endCalendarDate;

    private TextView reverseResultTv;

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

    private Amount amount;
    private Result result;

    private boolean clearInputsClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Set main layout
        setContentView(R.layout.main_calc_activity);

        // Initialise toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_calc_toolbar);
        setSupportActionBar(toolbar);

        // Set toolbar logo
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.sa_round_icon_logo);
        }

        // Initialise the amount object
        amount = new Amount(0);

        // Initialise dates
        currentCalendarDate = Calendar.getInstance(TimeZone.getDefault());
        startCalendarDate = Calendar.getInstance(TimeZone.getDefault());
        endCalendarDate = Calendar.getInstance(TimeZone.getDefault());

        // Get the current date and set is as the default end date
        currentYear = endYear = currentCalendarDate.get(Calendar.YEAR);
        currentMonth = endMonth= currentCalendarDate.get(Calendar.MONTH) + 1; // Java Calendar months start at 0 (Jan)
        currentDay = endDay = currentCalendarDate.get(Calendar.DAY_OF_MONTH);

        // Initialise various edit text boxes
        startDateEditText = (EditText) findViewById(R.id.enter_start_date_edit_text);
        endDateEditText = (EditText) findViewById(R.id.enter_end_date_edit_text);
        amountEditText = (EditText) findViewById(R.id.enter_amount_edit_text);
        resultEditText = (EditText) findViewById(R.id.result_edit_text);

        // Initialise date pickers
        startDatePicker = new DatePickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        endDatePicker = new DatePickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        // Initialise "Reverse result" text view
        reverseResultTv = (TextView) findViewById(R.id.reverse_answer);

        // Define special symbol for SA Rand currency
        final String randSymbol = "R";

        // Used to insert the rand symbol first time the view is touched
        amountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!amountEditText.getText().toString().contains(randSymbol)) {
                        amountEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        amountEditText.setText(randSymbol);
                        amountEditText.setSelection(amountEditText.getText().length());
                        amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                }
                return false;
            }
        });

        amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    amountEditText.setSelection(amountEditText.getText().length());
                    amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        });

        amountEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Hide keyboard if user presses enter
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&  (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    InputMethodManager inputMethodManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });

        // Used to update the amount object value
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ignore, nothing needed to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ignore, nothing to see here - changing an edit text's content in this callback
                // will cause an infinite loop
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Ignore all this if the user deciced to clear everything
                if (clearInputsClicked) {
                    return;
                }

                String convertedAmount = s.toString();

                if (s.length() == 0) {
                    amountEditText.setText(randSymbol);
                    amountEditText.setSelection(amountEditText.getText().length());
                }

                if (s.length() > 0) {
                    // Clear answers if the starting amount changes
                    if (resultEditText.getText().length() > 0) {
                        resultEditText.setText("");
                        reverseResultTv.setVisibility(View.GONE);
                    }

                    // If no numerical value is entered, don't bother parsing the edit text input
                    if (convertedAmount.equals(randSymbol)) {
                        amount.setValue(0);
                        return;
                    }

                    int indexOfDecimal = convertedAmount.indexOf(".");

                    // Limit the decimals to only two
                    if (indexOfDecimal != -1) {
                        try {
                            String decimals = convertedAmount.substring(indexOfDecimal + 1);

                            LogUtil.d("Found decimals: " + decimals);

                            if (decimals.length() > 2) {
                                LogUtil.d("Current input is " + convertedAmount);
                                convertedAmount = convertedAmount.substring(0, indexOfDecimal + 3);

                                LogUtil.d("After conversion, the current input is " + convertedAmount);

                                amountEditText.setText(convertedAmount);
                                amountEditText.setSelection(amountEditText.getText().length());
                            }
                        } catch (Exception e) {
                            LogUtil.e("Error in checking the decimals of the amount", e);
                        }
                    }

                    // Remove the rand symbol before parsing the value
                    if (convertedAmount.contains(randSymbol)) {
                        convertedAmount = convertedAmount.replace(randSymbol, "");
                    }

                    try {
                        amount.setValue(Double.parseDouble(convertedAmount));
                    } catch (Exception e) {
                        LogUtil.e("Error in parsing the amount", e);
                    }
                }
            }
        });

        // We want 0 to appear before the day or month if it's less than 10 (e.g. 01/01/1960 meaning 1 Jan 1960)
        final String day = DateUtil.convertToPrecedingZero(endDay);
        final String month = DateUtil.convertToPrecedingZero(endMonth);
        final String year = String.valueOf(endYear);

        // Automatically set the end date to be today's date
        endDateEditText.setText(day + "/" + month + "/" + year);

        // Construct the end date object
        endDate = new EndDate(year, month, day);

        // Used to show the start date picker when the view is selected
        startDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    startDatePicker.setReference(START_DATE_EDIT_REF);

                    if (startDay > 0 && startMonth >= 0 && startYear > 0) {
                        startDatePicker.setDayOfMonth(startDay);
                        startDatePicker.setMonthOfYear(startMonth - 1); // Java Calendar months start at 0 (Jan)
                        startDatePicker.setYear(startYear);
                    }

                    startDatePicker.show();
                    return true;
                }
                return false;
            }
        });

        // Used to show the end date picker when the view is selected
        endDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endDatePicker.setReference(END_DATE_EDIT_REF);

                    if (endDay > 0 && endMonth > 0 && endYear > 0) {
                        endDatePicker.setDayOfMonth(endDay);
                        endDatePicker.setMonthOfYear(endMonth - 1); // Java Calendar months start at 0
                        endDatePicker.setYear(endYear);
                    }

                    endDatePicker.show();
                }

                return true;
            }
        });

        // Handle the selection of the start date
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
                                Toast.makeText(MainCalcActivity.this, "The start date obviously can't be after the future date :)", Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    } else if (startYear < 1960) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainCalcActivity.this, "Sorry, we only support dates starting from 1960 :(", Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }
                }

                // We want a preceding zero in the date ot month if it is less than 10
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

        // Handle the selection of the start date
        endDatePicker.addDatePickerDialogHandler(new DatePickerDialogFragment.DatePickerDialogHandler() {
            @Override
            public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
                int realEndMonthOfYear = monthOfYear + 1; // Jan starts at 0

                LogUtil.d("END_DATE: Reference= " + (reference == END_DATE_EDIT_REF ?
                        "END_DATE_PICKER" : "") + ", year = " + year + ", month = " + realEndMonthOfYear + ", day = " + dayOfMonth);

                endYear = year;
                endMonth = realEndMonthOfYear;
                endDay = dayOfMonth;

                endCalendarDate.set(Calendar.YEAR, year);
                endCalendarDate.set(Calendar.MONTH, monthOfYear);
                endCalendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (startDateEditText.getText().length() > 0) {
                    if (endCalendarDate.getTimeInMillis() < startCalendarDate.getTimeInMillis()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainCalcActivity.this, "Let's not go back into the past please :)", Toast.LENGTH_LONG).show();
                                endDatePicker.setYear(currentYear);
                                endDatePicker.setMonthOfYear(currentMonth);
                                endDatePicker.setDayOfMonth(currentDay);
                            }
                        });

                        return;
                    } else if (endCalendarDate.getTimeInMillis() > currentCalendarDate.getTimeInMillis()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainCalcActivity.this, "Whoa there, you're reaching too far." +
                                        "\n\nThis is the future that we are talking about here and we can't make predictions at the moment", Toast.LENGTH_LONG).show();
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

        // Initialise the calculate button
        calculateButton = (Button) findViewById(R.id.calculate_btn);
        calculateButton.setEnabled(false);

        // Handle the clicking of the calculate button
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountEditText.getText().length() == 0 || amount.getValue() <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainCalcActivity.this, "Please enter an amount in order to make the calculation", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    String realValue = amountEditText.getText().toString().contains(randSymbol) ?
                            amountEditText.getText().toString().replace(randSymbol, "") : amountEditText.getText().toString();

                    amount.setValue(Double.parseDouble(realValue));

                    if (startDate != null && endDate != null) {
                        RequestParams params = new RequestParams();
                        params.put("date1", startDate.getApiRepresentation());
                        params.put("date2", endDate.getApiRepresentation());
                        params.put("amount", amount.getValue());

                        LogUtil.d("Input params:" + params.toString());

                        // Do the GET result API call
                        RestClient.get(MainCalcActivity.this, RestClient.BASE_URL, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                                try {
                                    LogUtil.d("Result: " + response.toString());

                                    double answer = MathUtil.round(response.optDouble("Answer"), 2);
                                    double reverseAnswer = MathUtil.round(response.optDouble("AnswerReverse"), 2);

                                    result = new Result(answer, reverseAnswer);

                                    resultEditText.setText("R" + String.valueOf(result.getCurrentValue()));

                                    reverseResultTv.setText(StringUtil.format(getString(R.string.reverse_result_answer),
                                            "R" + String.valueOf(result.getReverseValue())));
                                    reverseResultTv.setVisibility(View.VISIBLE);

                                } catch (Exception e) {
                                    LogUtil.e("Error in parsing the Inflationcalc response", e);
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
        if (item.getItemId() == R.id.action_about) {

            final String msg = getString(R.string.about_message);

            final AlertDialog aboutDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.about_heading))
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .create();

            try {
                // Try and Linkify the message
                final SpannableString spannableMessage = new SpannableString(msg);
                Linkify.addLinks(spannableMessage, Linkify.ALL);

                aboutDialog.setMessage(spannableMessage);
                aboutDialog.show();

                // Make the links clickable
                ((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            } catch (Exception e) {
                aboutDialog.setMessage(msg);
                aboutDialog.show();
            }
        } else if (item.getItemId() == R.id.action_clear_inputs) {
            clearInputsClicked = true;

            startDateEditText.getEditableText().clear();
            endDateEditText.getEditableText().clear();
            amountEditText.getEditableText().clear();
            resultEditText.getEditableText().clear();

            // Make sure the input box is always number input type
            amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

            startDatePicker.setDayOfMonth(-1);
            startDatePicker.setMonthOfYear(-1);
            startDatePicker.setYear(-1);

            endDatePicker.setDayOfMonth(-1);
            endDatePicker.setMonthOfYear(-1);
            endDatePicker.setYear(-1);

            reverseResultTv.setVisibility(View.GONE);

            clearInputsClicked = false;
        }
        return super.onOptionsItemSelected(item);
    }
}
