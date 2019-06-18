package com.example.lotus.todoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * This class represents the alternative activity of the To-do Android application. It deals with
 * the handling of the user's input and connecting it with the user interface of the main activity,
 * EntryListActivity.
 *
 * @author Patrick Jung
 * @version June 17, 2019
 */

public class EntryAddActivity extends Activity {

    // Initialize instance constants and variables
    public static final String EMPTY_DESCRIPTION_MESSAGE = "Please enter a description.";
    public static final String CHECKED_DAILY_MESSAGE =
            "Notifications for this entry will now appear on a daily basis.";
    private EditText entryDescriptEditText;
    private Spinner entrySectionSpinner;
    private TimePicker entryTimePicker;
    private CheckBox entryDailyToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_add);

        // Refer input widgets to instance variables, sets event listeners for some of the widgets
        entryDescriptEditText = findViewById(R.id.entryDescriptEditText);
        entrySectionSpinner = findViewById(R.id.entrySectionSpinner);
        entryTimePicker = findViewById(R.id.entryTimePicker);
        entryDailyToggle = findViewById(R.id.entryDailyToggle);

        entryDescriptEditText.setOnFocusChangeListener(new EditableListener());
        entrySectionSpinner.setOnItemSelectedListener(new SpinnerListener());
        entryDailyToggle.setOnClickListener(new CheckBoxListener());

        // Creates and adapts the use of an ArrayAdapter for the spinner widget
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.entrySectionArray, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entrySectionSpinner.setAdapter(spinnerAdapter);
    }

    // This method is expected to execute upon pressing the "Finish" button in the add activity;
    // it deals with the events that take place as a result of the extracted input
    public void onEntryFinishClick(View view) {
        Intent intentForEntryList;
        String endDescription;
        String endSection;
        int selectedHour;
        int selectedMinute;
        boolean selectedDailyToggle;

        // Tests for a description text editor with existing text and handles it
        if (entryDescriptEditText.getText().toString().length() > 0) {

            // Enables an intent to return to EntryListActivity and to ultimately finish the activity
            intentForEntryList = new Intent(getBaseContext(), EntryListActivity.class);
            intentForEntryList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Extract input from input widgets including description, section, time, and daily check
            endDescription = entryDescriptEditText.getText().toString();
            endSection = entrySectionSpinner.getSelectedItem().toString();

            if (Build.VERSION.SDK_INT >= 23) {
                selectedHour = entryTimePicker.getHour();
                selectedMinute = entryTimePicker.getMinute();
            } else {
                selectedHour = entryTimePicker.getCurrentHour();
                selectedMinute = entryTimePicker.getCurrentMinute();
            }

            selectedDailyToggle = entryDailyToggle.isChecked();

            // Bundles the extracted input from EntryAddActivity for the EntryListActivity intent
            intentForEntryList.putExtra("ENTRY_DESCRIPTION", endDescription);
            intentForEntryList.putExtra("ENTRY_SECTION", endSection);
            intentForEntryList.putExtra("ENTRY_TIME_HOUR", selectedHour);
            intentForEntryList.putExtra("ENTRY_TIME_MINUTE", selectedMinute);
            intentForEntryList.putExtra("ENTRY_DAILY_TOGGLE", selectedDailyToggle);

            // The EntryAddActivity successfully finishes here with resulting input
            setResult(RESULT_OK, intentForEntryList);
            finish();
        } else {

            // For non-existing text in the description text editor: restrict the user from
            // finishing EntryAddActivity, and warns the user via Toast to enter a description
            Toast warnMessage =
                    Toast.makeText(getApplicationContext(), EMPTY_DESCRIPTION_MESSAGE, Toast.LENGTH_SHORT);
            warnMessage.show();
        }
    }

    // This method is expected to execute upon pressing the "Finish" button in the add activity;
    // it deals with the events to successfully exit out of it, without any input extraction
    public void onEntryCancelClick(View view) {

        // Enables an intent to return to EntryListActivity and to ultimately finish the activity
        Intent intentForEntryList = new Intent(getBaseContext(), EntryListActivity.class);
        intentForEntryList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // The EntryAddActivity successfully finishes here with no resulting input
        setResult(RESULT_CANCELED, intentForEntryList);
        finish();
    }

    // This method is used to hide the existing keyboard input
    public void hideKeyboard(View view) {

        // Hides any existing keyboard input upon input method
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException exception) {
            Log.d(null, "Log: Exception in hideKeyboard(): " + exception);
        }
    }

    // This inner class is used to handle item select events happening in an existing input spinner
    class SpinnerListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // This method is unused but must be overridden
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // This method is unused but must be overridden
        }
    }

    // This inner class is used to hide the keyboard by changing focus in an existing editable text
    class EditableListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {

            // Checks for loss of focus in the editable text, and if successful, hides the keyboard
            if (!hasFocus) {

                hideKeyboard(view);
            }
        }
    }

    // This inner class is used to notify the user upon pressing an existing check box
    class CheckBoxListener implements OnClickListener {

        @Override
        public void onClick(View view) {

            // Checks for presses on specifically the daily toggle, displaying a notifier via Toast
            if ((view.getId() == R.id.entryDailyToggle) && entryDailyToggle.isChecked()) {

                Toast checkedNotifier =
                        Toast.makeText(getApplicationContext(), CHECKED_DAILY_MESSAGE, Toast.LENGTH_SHORT);
                checkedNotifier.show();
            }
        }
    }
}