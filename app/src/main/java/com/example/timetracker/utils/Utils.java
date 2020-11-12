package com.example.timetracker.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.example.timetracker.R;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static Set<String> getEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet("EVENTS", new HashSet<String>());
    }

    public static void saveEvents(Context context, Set<String> events) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet("EVENTS", events)
                .apply();
    }

    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setupSystemUI(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.teal_200));
        } else {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.white, null));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }*/
}

