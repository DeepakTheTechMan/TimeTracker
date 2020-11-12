package com.example.timetracker.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.timetracker.MainActivity;
import com.example.timetracker.R;

public class NotificationUtils {

    public static final String EXTRA_IS_REPEATED = "IS_REPEATED";

    public static final String EXTRA_EVENT_TITLE = "EVENT_TITLE";

    public static final String EXTRA_EVENT_DATE = "EVENT_DATE";

    public static final String EXTRA_EVENT_ID = "EVENT_ID";

    public static final String EXTRA_NOTIFICATION_TITLE = "NOTIFICATION_TITLE";

    public static final String EXTRA_NOTIFICATION_CONTENT = "NOTIFICATION_CONTENT";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void buildNormalReminder(Context context, long date, String title, int type, int interval, String id) {

        long showDate;
        String dateUnit;

        switch (type) {
            case RemindType.SINGLE_DUE_DATE:
                showDate = date;
                dateUnit = context.getString(R.string.remind_unit_now);
                break;
            case RemindType.SINGLE_MIN:
                showDate = date - interval * DateTimeUtils.MINUTE;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_minute, interval, interval);
                break;
            case RemindType.SINGLE_HOUR:
                showDate = date - interval * DateTimeUtils.HOUR;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_hour, interval, interval);
                break;
            case RemindType.SINGLE_DAY:
                showDate = date - interval * DateTimeUtils.DAY;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_day, interval, interval);
                break;
            case RemindType.SINGLE_WEEK:
                showDate = date - interval * DateTimeUtils.WEEK;
                dateUnit = context.getResources().getQuantityString(R.plurals.remind_unit_week, interval, interval);
                break;
            default:
                showDate = date;
                dateUnit = context.getString(R.string.remind_unit_now);
        }

        createAlarmManager(context, date, title, id, showDate, dateUnit, false);
    }

    public static void buildOngoingReminder(Context context) {

    }

    public static void cancelReminder(Context context, String id) {
        ComponentName componentName = new ComponentName(context, "com.gwokhou.deadline.ReminderReceiver");
        Intent intent = new Intent();
        intent.setData(Uri.parse(id));
        intent.setComponent(componentName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void createAlarmManager(Context context, long date, String title, String id, long showDate, String dateUnit, boolean isRepeated) {
        ComponentName componentName = new ComponentName(context, "com.example.timetracker.utils.ReminderReceiver");
        Intent intent = new Intent();
        intent.setData(Uri.parse(id));
        intent.setComponent(componentName);
        intent.putExtra(EXTRA_IS_REPEATED, isRepeated);
        intent.putExtra(EXTRA_NOTIFICATION_TITLE, context.getString(R.string.remind_title, title));
        intent.putExtra(EXTRA_NOTIFICATION_CONTENT, DateTimeUtils.longToString(date, DateTimeUtils.MEDIUM));

        if (isRepeated) {
            intent.putExtra(EXTRA_EVENT_TITLE, title);
            intent.putExtra(EXTRA_EVENT_DATE, date);
            intent.putExtra(EXTRA_EVENT_ID, id);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, showDate, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, showDate, pendingIntent);
        }
    }

    public static void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        /*Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beep_3x);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();*/

        /*Notification notification = new NotificationCompat.Builder(context, MainActivity.EVENT_NOTIFICATION_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                //.setDefaults(Notification.DEFAULT_ALL)
                //.setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(1, notification);*/

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.beep_3x);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
        /*NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(MainActivity.EVENT_NOTIFICATION_ID,  context.getString(R.string.event_notification), NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription(content);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel( mChannel );
            }
        }*/

        //General code:
        NotificationCompat.Builder status = new NotificationCompat.Builder(context, MainActivity.EVENT_NOTIFICATION_ID);

        status.setDefaults(Notification.DEFAULT_SOUND);

        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(content)
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.beep_3x))
                .setContentIntent(pendingIntent);
        //.setContent(views);

        mNotificationManager.notify(1, status.build());

    }

}
