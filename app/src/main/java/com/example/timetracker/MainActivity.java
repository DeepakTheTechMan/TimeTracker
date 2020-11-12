package com.example.timetracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timetracker.utils.NotificationUtils;
import com.example.timetracker.utils.RemindType;
import com.example.timetracker.utils.ReminderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Dialog dDialog;
    private View min5ContainerView, min10ContainerView, min20ContainerView, min30ContainerView,
            min45ContainerView, min60ContainerView;

    private Button customReminderButton;
    private ImageButton upcomingReminderButton;

    public static final String EVENT_NOTIFICATION_ID = "EVENT_NOTIFICATION";
    private List<Event> events;
    private String name;

    private TextView nearestReminderMinute, nearestReminderSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getPackageName() + "/" + R.raw.beep_3x);

            NotificationChannel channel = new NotificationChannel(EVENT_NOTIFICATION_ID, getString(R.string.event_notification), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            channel.setLightColor(Color.GRAY);
            channel.enableLights(true);
            //channel.setDescription(name);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            manager.createNotificationChannel(channel);
        }

        findViews();
        setUpViews();
    }

    private void findViews(){
        min5ContainerView = (LinearLayout) findViewById(R.id.min5Container);
        min10ContainerView = (LinearLayout) findViewById(R.id.min10Container);
        min20ContainerView = (LinearLayout) findViewById(R.id.min20Container);
        min30ContainerView = (LinearLayout) findViewById(R.id.min30Container);
        min45ContainerView = (LinearLayout) findViewById(R.id.min45Container);
        min60ContainerView = (LinearLayout) findViewById(R.id.min60Container);

        customReminderButton = (Button) findViewById(R.id.customReminder);

        upcomingReminderButton = (ImageButton) findViewById(R.id.upcomingReminderButton);

        nearestReminderMinute = (TextView) findViewById(R.id.nearestReminderMinute);
        nearestReminderSecond = (TextView) findViewById(R.id.nearestReminderSecond);

        events = Paper.book().read("events");
        if(events == null) {
            events = new ArrayList<>();
            Paper.book().write("events", events);
        }

        //Toast.makeText(this, ""+events.size(), Toast.LENGTH_SHORT).show();
    }

    private void setUpViews(){
        min5ContainerView.setOnClickListener(this);
        min10ContainerView.setOnClickListener(this);
        min20ContainerView.setOnClickListener(this);
        min30ContainerView.setOnClickListener(this);
        min45ContainerView.setOnClickListener(this);
        min60ContainerView.setOnClickListener(this);

        customReminderButton.setOnClickListener(this);

        upcomingReminderButton.setOnClickListener(this);

        updateCountDownTimer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.min5Container:
                name = "Reminder After 5 Min";
                confirmDialogBox(5*60*1000, "Reminder Set For 5 Min!");
                break;
            case R.id.min10Container:
                name = "Reminder After 10 Min";
                confirmDialogBox(10*60*1000, "Reminder Set For 10 Min!");
                break;
            case R.id.min20Container:
                name = "Reminder After 20 Min";
                confirmDialogBox(20*60*1000, "Reminder Set For 20 Min!");
                break;
            case R.id.min30Container:
                name = "Reminder After 30 Min";
                confirmDialogBox(30*60*1000, "Reminder Set For 30 Min!");
                break;
            case R.id.min45Container:
                name = "Reminder After 45 Min";
                confirmDialogBox(45*60*1000, "Reminder Set For 45 Min!");
                break;
            case R.id.min60Container:
                name = "Reminder After 60 Min";
                confirmDialogBox(60*60*1000, "Reminder Set For 60 Min!");
                break;
            case R.id.customReminder:
                customReminderDialogBox();
                break;
            case R.id.upcomingReminderButton:
                showUpcomingReminderActivity();
                break;
        }
    }

    private void confirmDialogBox(final long reminderTime, final String msg){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            dDialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            dDialog = new Dialog(MainActivity.this);
        }
        dDialog.setContentView(R.layout.confirm_dialog);

        TextView yesButton = dDialog.findViewById(R.id.yesButton);
        TextView noButton = dDialog.findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                long endDate = System.currentTimeMillis() + reminderTime;

                if (endDate - System.currentTimeMillis() < 0) {
                    Toast.makeText(getApplicationContext(),"End Date cannot be in Past",Toast.LENGTH_LONG).show();
                    return;
                }
                String eventId = String.valueOf(endDate);
                events.add(new Event(name, "", 0, endDate, StateType.ONGOING, false, 0, eventId, 0, "", System.currentTimeMillis()));
                NotificationUtils.buildNormalReminder(getApplication(), endDate, name, RemindType.SINGLE_DUE_DATE, ReminderUtils.getSingleRemindInterval(RemindType.SINGLE_DUE_DATE), eventId);
                Paper.book().write("events", events);
                updateCountDownTimer();
                //Toast.makeText(MainActivity.this, ""+events.size(), Toast.LENGTH_SHORT).show();
                //refreshList(events);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                if (dDialog != null){
                    dDialog.cancel();
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dDialog != null){
                    dDialog.cancel();
                }
            }
        });

        dDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dDialog.show();
    }

    private void customReminderDialogBox(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            dDialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            dDialog = new Dialog(MainActivity.this);
        }
        dDialog.setContentView(R.layout.custom_reminder_dialog);

        Button setReminderButton = dDialog.findViewById(R.id.setReminder);
        final EditText durationEditText = dDialog.findViewById(R.id.duration);

        setReminderButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                String duration = durationEditText.getText().toString();

                if (duration.equals("")){
                    Toast.makeText(getApplicationContext(),"Must Input Duration!",Toast.LENGTH_LONG).show();
                    return;
                }

                if (Integer.parseInt(duration) <= 0){
                    Toast.makeText(getApplicationContext(),"Duration Can't Be Zero / Less Than Zero!",Toast.LENGTH_LONG).show();
                    return;
                }

                if (Integer.parseInt(duration) > 60){
                    Toast.makeText(getApplicationContext(),"Duration Must Be Less Than 60 Minutes!",Toast.LENGTH_LONG).show();
                    return;
                }

                long endDate = System.currentTimeMillis() + (Integer.parseInt(duration) * 60 * 1000);

                if (endDate - System.currentTimeMillis() < 0) {
                    Toast.makeText(getApplicationContext(),"End Date cannot be in Past",Toast.LENGTH_LONG).show();
                    return;
                }

                name = "Reminder After " + duration + " Min";

                String eventId = String.valueOf(endDate);
                events.add(new Event(name, "", 0, endDate, StateType.ONGOING, false, 0, eventId, 0, "", System.currentTimeMillis()));
                NotificationUtils.buildNormalReminder(getApplication(), endDate, name, RemindType.SINGLE_DUE_DATE, ReminderUtils.getSingleRemindInterval(RemindType.SINGLE_DUE_DATE), eventId);
                Paper.book().write("events", events);
                updateCountDownTimer();
                //refreshList(events);
                Toast.makeText(MainActivity.this, "Reminder Set For " + duration + " Min!", Toast.LENGTH_SHORT).show();

                if (dDialog != null){
                    dDialog.cancel();
                }
            }
        });

        /*noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dDialog != null){
                    dDialog.cancel();
                }
            }
        });*/

        dDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dDialog.show();
    }

    private void showUpcomingReminderActivity() {
        Intent intent = new Intent(MainActivity.this, ActivityUpcomingReminder.class);
        startActivity(intent);
    }

    private void updateCountDownTimer(){
        nearestReminderMinute.setText("00:");
        nearestReminderSecond.setText("00");
        findViewById(R.id.rotateCircle).clearAnimation();

        if (events != null && events.size() != 0){
            List<Long> longList = new ArrayList<>();

            for (int i = 0; i < events.size(); i++) {
                Long endDateMillis = events.get(i).getEndDate();
                longList.add(endDateMillis);
            }
            long min = Long.MAX_VALUE;
            long minValue = 0;

            for (int i = 0; i < longList.size(); i++) {

                if (longList.get(i) < min) {
                    min = longList.get(i);
                }
            }
            //minValue = /*String.format("%.1f", min)*/ min - System.currentTimeMillis();

            if (min - System.currentTimeMillis() > 0){
                minValue = min - System.currentTimeMillis();
            }/*else {
                nearestReminder.setText("00:00");
            }*/

            final long finalMin = min;
            new CountDownTimer(minValue, 1000){
                @SuppressLint({"SetTextI18n", "DefaultLocale"})
                @Override
                public void onTick(long millisUntilFinished) {
                    String minute = "00";
                    String sec = "00";

                    if (TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished) <= 9){
                        minute = "0" + TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished);
                    }else {
                        minute = "" + TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished);
                    }

                    if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)) <= 9){
                        sec = "0" + (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    }else {
                        sec = "" + (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    }

                    nearestReminderMinute.setText(minute + ":");
                    nearestReminderSecond.setText(sec);

                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_around_center_point);
                    findViewById(R.id.rotateCircle).startAnimation(animation);
                }

                @Override
                public void onFinish() {
                    nearestReminderMinute.setText("00:");
                    nearestReminderSecond.setText("00");
                    findViewById(R.id.rotateCircle).clearAnimation();

                    deleteEvent(String.valueOf(finalMin));

                    if (events != null && events.size() != 0){
                        updateCountDownTimer();
                    }
                }
            }.start();
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
}