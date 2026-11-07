package com.example.chieuthu3_ketthucmonhoc;

import android.app.Application;

import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppServiceLocator.init(this);
    }
}
