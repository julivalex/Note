package com.julivalex.note;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by julivalex on 03.09.17.
 */

public class PreferenceHelper {

    public static final String SPLASH_IS_INVISIBLE = "splash_is_invisible";

    private static PreferenceHelper inctance;

    private Context context;
    private SharedPreferences sharedPreferences;

    private PreferenceHelper() {

    }

    public static PreferenceHelper getInctance() {
        if(inctance == null) {
            inctance = new PreferenceHelper();
        }
        return inctance;
    }

    public void init(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}
