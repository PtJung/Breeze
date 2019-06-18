package com.example.lotus.todoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class represents the alarm of the To-do Android application. It connects to
 * EntryListActivity in such a way that it can simulate an alarm as a result of an expected loop.
 * An AlarmForEntry object has access to its duration, daily, deletion, and looped attributes.
 *
 * @author Patrick Jung
 * @version June 17, 2019
 */

public class AlarmForEntry extends BroadcastReceiver {

    // Initialize instance constants and variables
    public static final int SECONDS_IN_A_DAY = 86400;
    private boolean recentlyLooped;
    private boolean dailyToggle;
    private boolean upForDeletion;
    private int seconds;

    // This method is the no-argument constructor for this class
    public AlarmForEntry() {

        // Initialize a turned-off alarm with an invalid number of seconds
        seconds = -1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is unused but must be overridden
    }

    // This method must be globally executed every second for this class to simulate a timer
    public void tickBySecond() {
        if (seconds > 0) {

            // If time is available for this object: tick down for each passed second
            seconds--;
        } else if (seconds == 0) {

            // If the object meets the time for its alarm to go off: loop if daily, and set a flag
            // for its deletion if its a non-daily timer
            setUpForDeletion(!dailyToggle);
            if (dailyToggle) {

                // Daily toggle: repeat on a daily basis; set a flag for activities to notify
                setRecentlyLooped(true);
                seconds += (SECONDS_IN_A_DAY - 1);
            }
        }
    }

    // These accessor methods return their respective instance variable values
    public int getSeconds() {
        return seconds;
    }

    // These mutator methods sets parameterized values for their respective instance variables
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public boolean getUpForDeletion() {
        return upForDeletion;
    }

    public void setUpForDeletion(boolean upForDeletion) {
        this.upForDeletion = upForDeletion;
    }

    public boolean getDailyToggle() {
        return dailyToggle;
    }

    public void setDailyToggle(boolean dailyToggle) {
        this.dailyToggle = dailyToggle;
    }

    public boolean getRecentlyLooped() {
        return recentlyLooped;
    }

    public void setRecentlyLooped(boolean recentlyLooped) {
        this.recentlyLooped = recentlyLooped;
    }

    @Override
    public String toString() {

        // Returns a String representation of this object
        return String.format("AlarmForEntry:{duratn:\"%ds\",delete:\"%b\",daily:\"%b\",looped:\"%b\"}",
                getSeconds(), getUpForDeletion(), getDailyToggle(), getRecentlyLooped());
    }
}