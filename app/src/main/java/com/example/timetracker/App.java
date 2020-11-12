package com.example.timetracker;

import android.app.Application;

import io.paperdb.Paper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }
}
