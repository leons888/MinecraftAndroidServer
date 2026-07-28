package com.bdscontrol.app;

import android.content.Context;
import android.content.SharedPreferences;

final class AppConfig {
    private final SharedPreferences p;
    AppConfig(Context c){ p=c.getSharedPreferences("bds_control",Context.MODE_PRIVATE); }
    String get(String key,String fallback){return p.getString(key,fallback);}
    void put(String key,String value){p.edit().putString(key,value).apply();}
    int getInt(String key,int fallback){return p.getInt(key,fallback);}
    void putInt(String key,int value){p.edit().putInt(key,value).apply();}
}
