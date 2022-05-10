package com.example.arknightstranslator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    Context context;
    private Map settingsMap;
    private SharedPreferences.Editor editor;

    public Preferences(Context context)
    {
        this.context = context;
        readPrefs();
    }

    private void readPrefs()    //read or update preferences
    {
        SharedPreferences settings = context.getSharedPreferences("PreferencesName", MODE_PRIVATE);
        editor = settings.edit();
        settingsMap = context.getSharedPreferences("PreferencesName", MODE_PRIVATE).getAll();
    }

    public void savePrefs()
    {
        editor.commit();
        readPrefs();
    }

    public String getKey() {
        return (String) settingsMap.get("x-key");
    }
    public void setKey(String key) {
        editor.putString("x-key", key);
    }

    public void setDoTranslate(boolean isDoTranslate) {
        settingsMap.put("isDoTranslate",isDoTranslate);
    }
    public boolean getDoTranslate()
    {
        return (boolean) settingsMap.get("isDoTranslate");
    }


}
