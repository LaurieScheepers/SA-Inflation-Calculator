package za.co.inflationcalc.ui.activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.codetroopers.betterpickers.datepicker.DatePickerDialogFragment;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.Calendar;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;
import za.co.inflationcalc.R;
import za.co.inflationcalc.comms.http.RestClient;
import za.co.inflationcalc.model.Amount;
import za.co.inflationcalc.model.EndDate;
import za.co.inflationcalc.model.Result;
import za.co.inflationcalc.model.StartDate;
import za.co.inflationcalc.utils.DateUtil;
import za.co.inflationcalc.utils.KeyboardUtil;
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

    // Identifiers for the date pickers
    public static final int START_DATE_EDIT_REF = 0;
    public static final int END_DATE_EDIT_REF = 1;

    // Identifiers for the parcelable objects
    public static final String EXTRA_SHOULD_RESTORE_STATE_FLAG = "should_restore_state_flag";
    public static final String EXTRA_START_DATE = "extra_start_date";
    public static final String EXTRA_END_DATE = "extra_end_date";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_RESULT = "extra_result";

    // Define special symbol for SA Rand currency
    public static final String RAND_SYMBOL = "R";

    // Edit text input fields
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText amountEditText;
    private EditText resultEditText;

    // Date picker
    private DatePickerBuilder startDatePicker;
    private DatePickerBuilder endDatePicker;

    // Calendars representing the different dates we want
    private Calendar currentCalendarDate;
    private Calendar startCalendarDate;
    private Calendar endCalendarDate;

    // A textview containing the "reverse answer"
    private TextView reverseResultTv;

    // The button to press to do the calculation
    private Button calculateButton;

    // Date objects
    private StartDate startDate;
    private EndDate endDate;

    // TODO clean up later. Not necessary for so many member variables
    private int currentYear;
    private int currentMonth;
    private int currentDay;

    private int endYear;
    private int endMonth;
    private int endDay;

    private int startYear;
    private int startMonth;
    private int startDay;

    // Objects indicating the amount that was entered and also the result obtained after
    // the inflated value was calculated
    private Amount amount;
    private Result result;

    // Flag indicating that state is being restored after the activity is re-created
    private boolean stateIsBeingRestored;

    // Flag indicating that the users cleared all inputs
    private boolean clearInputsClicked;

    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Set main layout
        setContentView(R.layout.main_calc_activity);

        // Keep reference to the root layout
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // If that didn't work, try this
        if (rootView == null) {
            rootView = getWindow().getDecorView().findViewById(android.R.id.content).getRootView();
        }

        // Initialise toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_calc_toolbar);
        setSupportActionBar(toolbar);

        // Set toolbar logo
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.sa_round_icon_logo);
        }

        if (savedInstanceState != null) {
            stateIsBeingRestored = savedInstanceState.getBoolean(EXTRA_SHOULD_RESTORE_STATE_FLAG);

            startDate = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_START_DATE));
            endDate = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_END_DATE));
            amount = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_AMOUNT));

            LogUtil.d("Deserializing Result Object");

            result = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_RESULT));

            if (result != null) {
                LogUtil.d("Result current value is: " + result.getCurrentValue());
                LogUtil.d("Result reverse value is: " + result.getReverseValue());
            }

            if (startDate != null) {
                startDay = Integer.parseInt(startDate.getDay());
                startMonth = Integer.parseInt(startDate.getMonth());
                startYear = Integer.parseInt(startDate.getYear());
            }

            if (endDate != null) {
                endDay = Integer.parseInt(endDate.getDay());
                endMonth = Integer.parseInt(endDate.getMonth());
                endYear = Integer.parseInt(endDate.getYear());
            }
        }

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

        // Initialise the amount object
        if (amount == null) {
            amount = new Amount(0);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    amountEditText.setText(RAND_SYMBOL + String.valueOf(amount.getValue()));
                }
            });
        }

        if (result != null) {
            LogUtil.d("Retrieved the result object");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d("Setting the result text with value " + result.getCurrentValue());
                    resultEditText.setText(RAND_SYMBOL + String.valueOf(result.getCurrentValue()));

                    LogUtil.d("Setting the reverse result text with value " + result.getReverseValue());

                    reverseResultTv.setText(StringUtil.format(getString(R.string.reverse_result_answer),
                            RAND_SYMBOL + String.valueOf(result.getReverseValue())));

                    reverseResultTv.setVisibility(View.VISIBLE);
                }
            });
        }

        // Used to insert the rand symbol first time the view is touched
        amountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!amountEditText.getText().toString().contains(RAND_SYMBOL)) {
                        amountEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        amountEditText.setText(RAND_SYMBOL);
                        amountEditText.setSelection(amountEditText.getText().length());
                        amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
                    amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            }
        });

        amountEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Hide keyboard if user presses enter
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&  (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    KeyboardUtil.hideKeyboard(getApplicationContext(), amountEditText);
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
                // Ignore all this if the user decided to clear everything
                if (clearInputsClicked) {
                    return;
                }

                String convertedAmount = s.toString();

                if (s.length() == 0) {
                    amountEditText.setText(RAND_SYMBOL);
                    amountEditText.setSelection(amountEditText.getText().length());
                }

                if (s.length() > 0) {
                    // Clear answers if the starting amount changes (only to be done if the activity is not re-created
                    if (resultEditText.getText().length() > 0 && !stateIsBeingRestored) {
                        LogUtil.d("The amount changed: clear the answer views");
                        resultEditText.setText("");
                        reverseResultTv.setVisibility(View.GONE);
                    }

                    // If no numerical value is entered, don't bother parsing the edit text input
                    if (convertedAmount.equals(RAND_SYMBOL)) {
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
                    if (convertedAmount.contains(RAND_SYMBOL)) {
                        convertedAmount = convertedAmount.replace(RAND_SYMBOL, "");
                    }

                    try {
                        amount.setValue(Double.parseDouble(convertedAmount));
                    } catch (Exception e) {
                        LogUtil.e("Error in parsing the amount", e);
                    }
                }

                stateIsBeingRestored = false;
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

                if (startDateEditText.getEditableText().length() > 0 && endDateEditText.getEditableText().length() > 0) {
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

                if (startDateEditText.getEditableText().length() > 0 && endDateEditText.getEditableText().length() > 0) {
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
                    String realValue = amountEditText.getText().toString().contains(RAND_SYMBOL) ?
                            amountEditText.getText().toString().replace(RAND_SYMBOL, "") : amountEditText.getText().toString();

                    amount.setValue(Double.parseDouble(realValue));

                    if (startDate == null) {
                        throw new IllegalArgumentException("Start Date can't be null");
                    }

                    if (startDate == null) {
                        throw new IllegalArgumentException("End Date can't be null");
                    }

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

                                    resultEditText.setText(RAND_SYMBOL + String.valueOf(result.getCurrentValue()));

                                    reverseResultTv.setText(StringUtil.format(getString(R.string.reverse_result_answer),
                                            "R" + String.valueOf(result.getReverseValue())));
                                    reverseResultTv.setVisibility(View.VISIBLE);

                                    // Remove focus from the "amount" field to the root view
                                    if (rootView != null) {
                                        rootView.requestFocus();
                                    }

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
                    } else {
                        Toast.makeText(getApplicationContext(), "Error in calculating the result, check if all the fields are filled in", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        clearInputsClicked = false;

        // Make sure the edit text is set to numeric input type
        amountEditText.setSelection(amountEditText.getText().length());
        amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (startDateEditText.getEditableText().length() > 0 && endDateEditText.getEditableText().length() > 0) {
            calculateButton.setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Parcels
        final Parcelable wrappedStartDate;
        final Parcelable wrappedEndDate;
        final Parcelable wrappedAmount;
        final Parcelable wrappedResult;

        savedInstanceState.putBoolean(EXTRA_SHOULD_RESTORE_STATE_FLAG, true);

        if (startDate != null) {
            wrappedStartDate = Parcels.wrap(startDate);
            savedInstanceState.putParcelable(EXTRA_START_DATE, wrappedStartDate);
        }

        if (endDate != null) {
            wrappedEndDate = Parcels.wrap(endDate);
            savedInstanceState.putParcelable(EXTRA_END_DATE, wrappedEndDate);
        }

        if (amount != null) {
            wrappedAmount = Parcels.wrap(amount);
            savedInstanceState.putParcelable(EXTRA_AMOUNT, wrappedAmount);
        }

        if (result != null) {
            LogUtil.d("Serializing Result Object");
            wrappedResult = Parcels.wrap(result);
            savedInstanceState.putParcelable(EXTRA_RESULT, wrappedResult);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (rootView != null) {
            LogUtil.d("Resetting focus to the root layout");
            rootView.requestFocus();
        }

        // also hide the keyboard whenever screen orientation changes

        KeyboardUtil.hideKeyboard(getApplicationContext(), amountEditText);
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
                Linkify.addLinks(spannableMessage, Linkify.WEB_URLS);

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
            amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

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
