package com.example.lotus.todoapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class represents the main activity of the To-do Android application. It deals with handling
 * the application's main events, displaying all of the valid entries, and connects to the
 * alternative activity, EntryAddActivity.
 *
 * @author Patrick Jung
 * @version June 17, 2019
 */

public class EntryListActivity extends Activity {

    // Initialize instance constants and variables
    public static final String MAX_ENTRIES_REACHED_MESSAGE =
            "Unable to add an entry: maximum entries have been reached.";
    public static final int MAX_ENTRIES = 6;
    public static final int REQUEST_CODE = 1;
    private NotificationCompat.Builder entryNotifierBuilder;
    private Vibrator alarmVibrator;
    private CountDownTimer activityLoop;
    private AlarmForEntry[] alarmList = new AlarmForEntry[MAX_ENTRIES];
    private Entry[] entryList = new Entry[MAX_ENTRIES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
        createNotificationChannel();

        // Initialize entry and alarm lists with default instantiations
        for (int entryIndex = 0; entryIndex < MAX_ENTRIES; entryIndex++) {
            entryList[entryIndex] = new Entry();
            alarmList[entryIndex] = new AlarmForEntry();
        }

        // Creates and enables the use of vibration and push notifications for notification purposes
        alarmVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        entryNotifierBuilder =
                new NotificationCompat.Builder(this, "ENTRY_PUSH_NOTIFICATION")
                        .setSmallIcon(R.drawable.icon_todo_list)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        // Begins the EntryListActivity's main code loop and initializes the entry list display
        activityLoop = new ActivityLoop().start();
        updateEntryList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Calendar calendar;
        Entry newEntry;
        String entryDescription;
        String entrySection;
        boolean entryDailyToggle;
        int entryTimeHour;
        int entryTimeMinute;
        int entryIndex;
        int currentHour;
        int currentMinute;
        int currentSecond;
        int secondsUntilAlarm;

        if ((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)) {

            // Extracts the variable information from the resulting intent of EntryAddActivity
            entryDescription = resultIntent.getStringExtra("ENTRY_DESCRIPTION");
            entrySection = resultIntent.getStringExtra("ENTRY_SECTION");
            entryTimeHour = resultIntent.getIntExtra("ENTRY_TIME_HOUR", 0);
            entryTimeMinute = resultIntent.getIntExtra("ENTRY_TIME_MINUTE", 0);
            entryDailyToggle = resultIntent.getBooleanExtra("ENTRY_DAILY_TOGGLE", false);

            // Instantiates and appends a new entry to the list, made of the extracted information
            newEntry = new Entry(entryDescription, entrySection, entryTimeHour, entryTimeMinute);
            entryIndex = addEntry(newEntry);

            // Calculates the duration in seconds, from current time to the extracted alarm time
            calendar = Calendar.getInstance();
            currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            currentMinute = calendar.get(Calendar.MINUTE);
            currentSecond = calendar.get(Calendar.SECOND);
            secondsUntilAlarm = getDifferenceInSeconds(currentHour, currentMinute,
                    currentSecond, entryTimeHour, entryTimeMinute, 0);

            // Sets duration and daily attributes for the entry's respective alarm and updates list
            alarmList[entryIndex].setSeconds(secondsUntilAlarm);
            alarmList[entryIndex].setDailyToggle(entryDailyToggle);
            updateEntryList();
        }
    }

    // This method is used to generate a Scheduler push notification with a given title, text, and ID
    public void notifyFromScheduler(String paramTitle, String paramText, int notifierID) {

        //
        entryNotifierBuilder.setContentTitle(paramTitle);
        entryNotifierBuilder.setContentText(paramText);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notifierID, entryNotifierBuilder.build());
    }

    // This method is used to calculate the number of seconds from one time to another time
    public int getDifferenceInSeconds(int hourValue1, int minuteValue1, int secondValue1,
                                      int hourValue2, int minuteValue2, int secondValue2) {
        final int SECONDS_IN_DAY = 86400;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time1 = String.format("%02d:%02d:%02d", hourValue1, minuteValue1, secondValue1);
        String time2 = String.format("%02d:%02d:%02d", hourValue2, minuteValue2, secondValue2);
        Date date1;
        Date date2;
        int durationInSeconds;

        // Try-catch clause used for parsing time and handling possible exceptions
        try {

            // Calculate number of seconds in the duration between the two dates
            date1 = timeFormat.parse(time1);
            date2 = timeFormat.parse(time2);

            durationInSeconds = (int) (Math.abs(date2.getTime() - date1.getTime()) / 1000);

            // Compensate for first time being after second time, meaning duration lasts to next day
            if (date1.after(date2)) {
                durationInSeconds = SECONDS_IN_DAY - durationInSeconds;
            }

            // Returns the difference of time duration in seconds
            return durationInSeconds;
        } catch (ParseException exception) {

            // Returns an invalid duration in seconds, used to indicate the caught exception
            Log.d(null, "Log: Error in getDifferenceInSeconds(): " + exception);
            return (-1);
        }
    }

    // This method is expected to be executed upon pressing an existing button that adds an entry;
    // it deals with the intent to move from EntryListActivity to EntryAddActivity without finishing
    public void onEntryAddClick(View view) {
        if (!isEntryListFull()) {

            // If the entry list is not full, an entry can be made
            Intent intentForEntryAdd = new Intent(getBaseContext(), EntryAddActivity.class);
            startActivityForResult(intentForEntryAdd, REQUEST_CODE);
        } else {

            // Entry list is full; restrict and notify the user via a Toast message
            Toast warnMessage =
                    Toast.makeText(getApplicationContext(), MAX_ENTRIES_REACHED_MESSAGE, Toast.LENGTH_SHORT);
            warnMessage.show();
        }
    }

    // This method is expected to be executed upon pressing an existing button that removes an entry;
    // it deals with deleting an entry respective to its delete button and reports the deletion
    public void onEntryDeleteClick(View view) {
        Toast warnMessage;
        String entryRemovalMessage;
        int entryIndex;

        // Creates the text for the Toast message used to report its deletion from its entry index
        entryIndex = Integer.parseInt(view.getTag().toString()) - 1;
        entryRemovalMessage = String.format("Entry #%d has been removed.", entryIndex + 1);

        // Warns the user via a Toast message with the created text
        warnMessage = Toast.makeText(getApplicationContext(), entryRemovalMessage, Toast.LENGTH_SHORT);
        warnMessage.show();

        // Immediately deletes the entry and its respective alarm
        alarmList[entryIndex].setUpForDeletion(true);
        checkAlarmsForDeletion(false);
    }

    // This method is expected to be used on a repetitive loop for each second; it deals with
    // deleting any useless alarms and their respective entries, updates the list accordingly,
    // and uses notifications to notify an alarm time being met, given a true 'notifyFromScheduler'
    public void checkAlarmsForDeletion(boolean notifyFromScheduler) {
        Entry entry;
        String contentTitle;
        String contentText;

        for (int entryIndex = 0; entryIndex < MAX_ENTRIES; entryIndex++) {

            // Iterates through the alarm list and checks for valid alarms to determine the next step
            Log.d(null, "Log: " + alarmList[entryIndex].toString());
            if (alarmList[entryIndex] != null) {

                // The state of the alarm (non-daily, daily, manually deleted) determines the next step
                if (!alarmList[entryIndex].getDailyToggle() && alarmList[entryIndex].getUpForDeletion() &&
                        notifyFromScheduler) {

                    // For non-daily entries with alarm met: delete the entry and alarm
                    entry = popEntry(entryIndex);
                    alarmList[entryIndex] = new AlarmForEntry();

                    // Immediately creates notifications: push notification and vibration
                    contentTitle = "Scheduler" + String.format(" (%s)", entry.getEntrySection());
                    contentText = entry.getEntryDescription().trim();
                    notifyFromScheduler(contentTitle, contentText, entryIndex);
                    alarmVibrator.vibrate(2000);

                } else if (alarmList[entryIndex].getDailyToggle() && alarmList[entryIndex].getRecentlyLooped() &&
                        notifyFromScheduler) {

                    // For daily entries with alarm met: no automatic entry deletion
                    entry = entryList[entryIndex];

                    // Immediately creates notifications: push notification and vibration
                    alarmList[entryIndex].setRecentlyLooped(false);
                    contentTitle = "Scheduler" + String.format(" (Daily %s)", entry.getEntrySection());
                    contentText = entry.getEntryDescription().trim();
                    notifyFromScheduler(contentTitle, contentText, entryIndex);
                    alarmVibrator.vibrate(2000);
                } else if (alarmList[entryIndex].getUpForDeletion()) {

                    // For non-daily entries being deleted via button: deletes the entry and alarm without notifying
                    popEntry(entryIndex);
                    alarmList[entryIndex] = new AlarmForEntry();
                }
            }
        }

        // Updates the entry list
        updateEntryList();
    }

    // This method helps organize the entry list by collapsing, sorting, and optimizing the entries
    public void sanitizeEntries() {
        final int SECONDS_TO_OFFSET_ERROR = 5;

        // Iterates through the array list and collapses them, such that there are no spaces in between
        for (int entryIndex = 0; entryIndex < (MAX_ENTRIES - 1); entryIndex++) {
            if ((alarmList[entryIndex] != null) && (alarmList[entryIndex + 1] != null) &&
                    !entryList[entryIndex].getIsEntryActive() &&
                    entryList[entryIndex + 1].getIsEntryActive()) {

                // Successful collapse of one entry onto an empty entry space
                swapEntries(entryIndex, entryIndex + 1);
            }
        }

        // Iterates through the entry list and simply sorts the entries by durations
        for (int passCount = 0; passCount < (MAX_ENTRIES - 2); passCount++) {
            for (int entryIndex = 0; entryIndex < (MAX_ENTRIES - 1); entryIndex++) {

                // Checks if the entries in the iterated indexes exist
                if ((alarmList[entryIndex] != null) && (alarmList[entryIndex + 1] != null) &&
                        entryList[entryIndex].getIsEntryActive() &&
                        entryList[entryIndex + 1].getIsEntryActive()) {

                    // Comparison of alarms through their duration attributes
                    AlarmForEntry alarmFirst = alarmList[entryIndex];
                    AlarmForEntry alarmSecond = alarmList[entryIndex + 1];

                    if (alarmFirst.getSeconds() > alarmSecond.getSeconds()) {

                        // Swap in order to sort the entries
                        swapEntries(entryIndex, entryIndex + 1);
                    }
                }
            }
        }

        // Ensure that any minor offsets of time are taken care of, by making their times equal
        for (int entryIndex = 0; entryIndex < MAX_ENTRIES; entryIndex++) {
            if ((alarmList[entryIndex] != null) && entryList[entryIndex].getIsEntryActive()) {

                // Iterates through the alarm list and finds alarms close to each other in duration
                for (int searchIndex = 0; searchIndex < MAX_ENTRIES; searchIndex++) {
                    if ((Math.abs(alarmList[entryIndex].getSeconds() -
                            alarmList[searchIndex].getSeconds()) < SECONDS_TO_OFFSET_ERROR) &&
                            entryList[searchIndex].getIsEntryActive()) {

                        // Put durations of close-duration alarms in equivalence
                        alarmList[searchIndex].setSeconds(alarmList[entryIndex].getSeconds());
                    }
                }
            }
        }
    }

    // This method adds an entry to the entry list, without adding an alarm to the respective index
    public int addEntry(Entry entry) {
        for (int entryIndex = 0; entryIndex < MAX_ENTRIES; entryIndex++) {

            // Finds first empty entry if possible
            if ((entryList[entryIndex] != null) && !entryList[entryIndex].getIsEntryActive()) {

                // Sets the entry details to the empty entry
                entryList[entryIndex].setEntryDescription(entry.getEntryDescription());
                entryList[entryIndex].setEntrySection(entry.getEntrySection());
                entryList[entryIndex].setEntryTimeHour(entry.getEntryTimeHour());
                entryList[entryIndex].setEntryTimeMinute(entry.getEntryTimeMinute());
                entryList[entryIndex].setIsEntryActive(true);

                // Returns the index of the list the entry was created in
                return entryIndex;
            }
        }

        // Returns an invalid index when the search does not find an index to add the entry to
        return (-1);
    }

    // This method removes an entry at a certain index in the entry list, and returns it
    public Entry popEntry(int entryIndex) {
        Entry currentEntry = entryList[entryIndex];
        entryList[entryIndex] = new Entry("", "", 0, 0);

        // Returns and replaces the current entry
        return currentEntry;
    }

    // This method checks if the entry list is full and returns a boolean value for how its fullness
    public boolean isEntryListFull() {
        for (int entryIndex = 0; entryIndex < entryList.length; entryIndex++) {

            // Iterate through array and check for any inactive (empty) entries
            if ((entryList[entryIndex] != null) && !entryList[entryIndex].getIsEntryActive()) {

                // Inactive (empty) entry found; entry list is not full; return this boolean value for a non-full entry list
                return false;
            }
        }

        // All entries are active; return this boolean value for a full entry list
        return true;
    }

    // This method updates the entry list through its display, programmatically
    public void updateEntryList() {
        final String DAILY_LABEL = "<b>(Daily)</b> ";
        Calendar calendar;
        int currentHour;
        int currentMinute;
        int currentSecond;
        int secondsUntilAlarm;
        int secondsUntilNextDay;

        // Create arrays in order to reference to the six entries
        TextView[] entryDescriptArray = {
                findViewById(R.id.entryDescriptLabel1),
                findViewById(R.id.entryDescriptLabel2),
                findViewById(R.id.entryDescriptLabel3),
                findViewById(R.id.entryDescriptLabel4),
                findViewById(R.id.entryDescriptLabel5),
                findViewById(R.id.entryDescriptLabel6)};

        TextView[] entrySectionArray = {
                findViewById(R.id.entrySectionLabel1),
                findViewById(R.id.entrySectionLabel2),
                findViewById(R.id.entrySectionLabel3),
                findViewById(R.id.entrySectionLabel4),
                findViewById(R.id.entrySectionLabel5),
                findViewById(R.id.entrySectionLabel6)};

        TextView[] entryTimeArray = {
                findViewById(R.id.entryTimeLabel1),
                findViewById(R.id.entryTimeLabel2),
                findViewById(R.id.entryTimeLabel3),
                findViewById(R.id.entryTimeLabel4),
                findViewById(R.id.entryTimeLabel5),
                findViewById(R.id.entryTimeLabel6)};

        ImageButton[] entryDeleteButtonArray = {
                findViewById(R.id.entryDeleteButton1),
                findViewById(R.id.entryDeleteButton2),
                findViewById(R.id.entryDeleteButton3),
                findViewById(R.id.entryDeleteButton4),
                findViewById(R.id.entryDeleteButton5),
                findViewById(R.id.entryDeleteButton6)};

        // Iterate through the entry list to find all valid entries
        for (int entryIndex = 0; entryIndex < MAX_ENTRIES; entryIndex++) {
            if ((entryList[entryIndex] != null) && entryList[entryIndex].getIsEntryActive()) {

                // Active entries: set labels of each entry to their respective text and button
                if (alarmList[entryIndex].getDailyToggle()) {

                    // For daily notifications: use a bold daily label with description
                    entryDescriptArray[entryIndex].setText(
                            Html.fromHtml(DAILY_LABEL + entryList[entryIndex].getEntryDescription()));
                } else {

                    // For non-daily notifications: use just the description
                    entryDescriptArray[entryIndex].setText(entryList[entryIndex].getEntryDescription());
                }

                // Calculates the duration in seconds, from current time to the start of next day
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                currentSecond = calendar.get(Calendar.SECOND);
                secondsUntilAlarm = alarmList[entryIndex].getSeconds();
                secondsUntilNextDay = getDifferenceInSeconds(currentHour, currentMinute,
                        currentSecond, 0, 0, 0);

                // Set colours of each entry to symbolize entries in the current day and the next day
                if (secondsUntilAlarm >= secondsUntilNextDay) {

                    // If next alarm is after start of next day, change label colour to a particular one
                    entryTimeArray[entryIndex].setTextColor(Color.parseColor("#4f4fff"));
                } else {

                    // If next alarm is before start of next day, change label colour to a particular one
                    entryTimeArray[entryIndex].setTextColor(Color.parseColor("#ff4f4f"));
                }

                // Sets the text properties of the entry's section and time, and visibility of delete button
                entrySectionArray[entryIndex].setText(entryList[entryIndex].getEntrySection());
                entryTimeArray[entryIndex].setText(entryList[entryIndex].getEntryTime());
                entryDeleteButtonArray[entryIndex].setVisibility(View.VISIBLE);
            } else {

                // Inactive entries: meant to show nothing
                entryDescriptArray[entryIndex].setText("");
                entrySectionArray[entryIndex].setText("");
                entryTimeArray[entryIndex].setText("");
                entryDeleteButtonArray[entryIndex].setVisibility(View.GONE);
            }
        }

        // Finally sanitizes the entries
        sanitizeEntries();
    }

    // This method allows entries of one index and another index to swap places, including the
    // swapping of their respective alarms
    private void swapEntries(int entryIndex1, int entryIndex2) {

        // Swap entries of two given indexes
        Entry entryTemp;
        entryTemp = entryList[entryIndex1];
        entryList[entryIndex1] = entryList[entryIndex2];
        entryList[entryIndex2] = entryTemp;

        // Swap respective alarms of two given indexes
        AlarmForEntry alarmForEntryTemp;
        alarmForEntryTemp = alarmList[entryIndex1];
        alarmList[entryIndex1] = alarmList[entryIndex2];
        alarmList[entryIndex2] = alarmForEntryTemp;
    }

    // This method is used to create a notification channel
    private void createNotificationChannel() {
        NotificationChannel notificationChannel;
        NotificationManager notificationManager;
        String channelName;
        String channelDescription;
        int importance;

        // The availability of the notification channel is based off build version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Sets the channel name and description, and importance attributes
            channelName = "CHANNEL_NAME";
            channelDescription = "CHANNEL_DESCRIPTION";
            importance = NotificationManager.IMPORTANCE_DEFAULT;

            // Instantiate a notification channel with the set attributes
            notificationChannel = new NotificationChannel(
                    "ENTRY_PUSH_NOTIFICATION", channelName, importance);
            notificationChannel.setDescription(channelDescription);

            // Register the channel with the system
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    // This inner class deals with the repetitive alarm-check and tick loop of EntryListActivity
    class ActivityLoop extends CountDownTimer {

        // This method is the no-argument constructor of this inner class
        public ActivityLoop() {

            // Calls upon the parent class, which is made to repeat every second
            super(1000, 20);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // This method is unused but must be overridden
        }

        @Override
        public void onFinish() {

            Log.d(null, "Log: onFinish() executed successfully");

            // Ticks a second for each on-finish of a second of application runtime, checks for
            // required alarm deletion
            for (int entryIndex = 0; entryIndex < 6; entryIndex++) {
                alarmList[entryIndex].tickBySecond();
            }
            checkAlarmsForDeletion(true);

            // Start the activity loop again to create a loop
            activityLoop.start();
        }
    }
}