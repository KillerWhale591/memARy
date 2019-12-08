package com.killerwhale.memary;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public class Preference {

    public static double postDistance;
    public static long arNumber;

    public static final String KEY_AR_RENDER_LIMIT = "ar_render_limit";
    public static final String KEY_POST_SEARCH_LIMIT = "post_search_limit";

    // AR variables
    public static int arRenderLimit = 10;
    // Post variables
    public static double postSearchRadius = 1.00;

    public static void setPostSearchRadius(double postSearchRadius) {
        Preference.postSearchRadius = postSearchRadius;
    }

    public static void setArRenderLimit(int arRenderLimit) {
        Preference.arRenderLimit = arRenderLimit;
    }

    public static void setPreferences(Context aContext) {
        // Set all app preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(aContext);
        postDistance = Double.parseDouble(sharedPreferences.getString("postDistancePreference", "1"));
        arNumber = Long.parseLong(sharedPreferences.getString("arNumberPreference", "5"));
        //
    }
}
