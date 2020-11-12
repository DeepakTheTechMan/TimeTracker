package com.example.timetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.paperdb.Paper;

public class ActivityUpcomingReminder extends AppCompatActivity {

    private EventsAdapter mEventsAdapter;
    private List<Event> events;
    private RecyclerView recyclerView;

    private LinearLayout mEmptyLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_history);

        findViews();
        setUpViews();
    }

    private void findViews(){
        mEmptyLayout = (LinearLayout) findViewById(R.id.layout_recycler_view_my_courses_empty);

        recyclerView = findViewById(R.id.upcomingReminderRecycleview);
        mEventsAdapter = new EventsAdapter(this);

        events = Paper.book().read("events");
    }

    private void setUpViews(){
        recyclerView.setAdapter(mEventsAdapter);
        //ItemTouchHelper touchHelper = new ItemTouchHelper(new EventTouchHelperCallback(mEventsAdapter));
        //touchHelper.attachToRecyclerView(recyclerView);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animationController);

        filterCompletedEvents();
        refreshList(events);

        if(events.size() <= 0) {
            /*events = new ArrayList<>();
            Paper.book().write("events", events);*/
            mEmptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            mEmptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        /*recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mEventsAdapter);*/

        //Toast.makeText(this, "" + mEventsAdapter.getItemCount(), Toast.LENGTH_SHORT).show();

        mEventsAdapter.setEventItemActionListener(new EventItemActionListener() {
            @Override
            public void onItemSwiped(String eventId) {
                /*deleteEvent(eventId);
                refreshList(events);*/
            }

            @Override
            public void onItemClicked(String eventId) {
                /*Intent intent = new Intent(ActivityUpcomingReminder.this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);*/
            }

            @Override
            public void onReminderComplete(String eventId) {
                deleteEvent(eventId);
                refreshList(events);

                if(events.size() <= 0) {
                    mEmptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else {
                    mEmptyLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void filterCompletedEvents() {
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getEndDate() < System.currentTimeMillis()) {
                deleteEvent(event.getId());
            }
        }
    }

    private void deleteEvent(String id) {
        boolean hasChanged = false;
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if(event.getId().equals(id)) {
                addToCompletedEvents(event);
                iterator.remove();
                hasChanged = true;
            }
        }
        if(hasChanged) {
            Paper.book().write("events", events);
        }
    }

    private void addToCompletedEvents(Event event) {
        List<Event> completedEvents = Paper.book().read("completed_events");
        if(completedEvents == null) {
            completedEvents = new ArrayList<>();
        }
        completedEvents.add(event);
        Paper.book().write("completed_events", completedEvents);
    }

    private void refreshList(List<Event> events) {
        mEventsAdapter.setEvents(events);
    }
}
