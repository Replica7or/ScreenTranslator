package com.example.arknightstranslator;

import android.content.Context;

public class PreferencesSingleton {

    private Preferences preferences;

    private static final PreferencesSingleton INSTANCE = new PreferencesSingleton();

    public static PreferencesSingleton getInstance(){
        return INSTANCE;
    }

    private PreferencesSingleton() { }

    public void initPreferences(Context context)
    {
        this.preferences = new Preferences(context);
    }

    public Preferences getPreferences()
    {
        return preferences;
    }
}
