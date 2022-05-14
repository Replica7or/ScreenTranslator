package com.example.arknightstranslator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

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

    public void setDoTranslate(boolean isDoTranslate) {
        //settingsMap.put("isDoTranslate",isDoTranslate);
    }
}
