package com.example.timetracker;

public interface EventItemActionListener {
    void onItemSwiped(String eventId);
    void onItemClicked(String eventId);
    void onReminderComplete(String eventId);
}
