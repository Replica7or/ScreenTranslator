package com.example.arknightstranslator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


public class Preferences {
    private Context context;
    private SharedPreferences prefs;

    public Preferences(Context context)
    {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public String getKey() {
        return prefs.getString("keyTranslate", "не установлено");
    }

}
