package com.example.lotus.todoapp;

/**
 * This class represents an entry in the To-do Android application. An Entry object supports having
 * a description, section, time, and an active state.
 *
 * @author Patrick Jung
 * @version June 17, 2019
 */

public class Entry {

    // Initialize instance constants and variables
    private String entryDescription;
    private String entrySection;
    private int entryTimeHour;
    private int entryTimeMinute;
    private boolean isEntryActive;

    // This method is the no-argument constructor for this class
    public Entry() {
        this("", "", 0, 0);
    }

    // This method is the parameterized constructor for this class
    public Entry(String inputDescription, String inputSection, int inputTimeHour, int inputTimeMinute) {

        // Initializes the entry with a parameterized description, section, and time (hour, minute)
        setEntryDescription(inputDescription);
        setEntrySection(inputSection);
        setEntryTime(inputTimeHour, inputTimeMinute);
    }

    // These accessor methods return their respective instance variable values
    public String getEntryDescription() {
        return entryDescription;
    }

    // These mutator methods sets parameterized values for their respective instance variables
    public void setEntryDescription(String entryDescription) {
        this.entryDescription = entryDescription;
    }

    public String getEntrySection() {
        return entrySection;
    }

    public void setEntrySection(String entrySection) {
        this.entrySection = entrySection;
    }

    public int getEntryTimeHour() {
        return entryTimeHour;
    }

    public void setEntryTimeHour(int entryTimeHour) {
        this.entryTimeHour = entryTimeHour;
    }

    public int getEntryTimeMinute() {
        return entryTimeMinute;
    }

    public void setEntryTimeMinute(int entryTimeMinute) {
        this.entryTimeMinute = entryTimeMinute;
    }

    public boolean getIsEntryActive() {
        return isEntryActive;
    }

    public void setIsEntryActive(boolean isEntryActive) {
        this.isEntryActive = isEntryActive;
    }

    // This method returns a String representation of this object's time
    public String getEntryTime() {
        String displayMode = (entryTimeHour < 12) ? "AM" : "PM";
        int displayHour = (entryTimeHour == 12 || entryTimeHour == 0) ? 12 : (entryTimeHour % 12);
        int displayMinute = entryTimeMinute;

        // Returns a String representation of this object's time, in the form "HH:mm {AM, PM}"
        return String.format("%d:%02d %s", displayHour, displayMinute, displayMode);
    }

    // This method sets this object's time with parameterized hour and minute values
    public void setEntryTime(int entryTimeHour, int entryTimeMinute) {

        // Sets the time by setting the values for this object's hour and minute values
        setEntryTimeHour(entryTimeHour);
        setEntryTimeMinute(entryTimeMinute);
    }

    @Override
    public String toString() {

        // Returns a String representation of this object
        return String.format("Entry:{descrpt:\"%s\",section:\"%s\",time:\"%s\",active:\"%b\"}",
                getEntryDescription(), getEntrySection(), getEntryTime(), getIsEntryActive());
    }
}