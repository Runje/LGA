package com.example.thomas.lga.Activities;

import android.app.Application;

import com.example.thomas.lga.LGA;

/**
 * Created by Thomas on 21.10.2015.
 */
public class LGAApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        LGA.getSingleton().init(this);
    }
}
