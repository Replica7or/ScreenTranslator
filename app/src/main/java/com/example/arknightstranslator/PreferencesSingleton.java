package com.example.arknightstranslator;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferencesSingleton {

    private static final PreferencesSingleton INSTANCE = new PreferencesSingleton();
    private Preferences preferences;

    public void initPreferences(Context context)
    {
        this.preferences = new Preferences(context);
    }

    public static PreferencesSingleton getInstance(){
        return INSTANCE;
    }

    private PreferencesSingleton() { }


    public Preferences getPreferences()
    {
        return preferences;
    }
}
