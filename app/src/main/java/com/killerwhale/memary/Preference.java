package com.killerwhale.memary;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public class Preference {

    public static double postDistance;
    public static long arNumber;

    public static void setPreferences(Context aContext) {
        // Set all app preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(aContext);
        postDistance = Double.parseDouble(sharedPreferences.getString("postDistancePreference", "1"));
        arNumber = Long.parseLong(sharedPreferences.getString("arNumberPreference", "5"));
        //
    }
}
