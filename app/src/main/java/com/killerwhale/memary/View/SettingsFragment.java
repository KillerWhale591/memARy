package com.killerwhale.memary.View;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.Toast;

import com.killerwhale.memary.R;

public class SettingsFragment extends PreferenceFragmentCompat {

//    private SwitchPreferenceCompat notification;
//    private CheckBoxPreference orientation;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);

//        orientation = (CheckBoxPreference) findPreference("orientations");
//
//        orientation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object o) {
//                if(o instanceof Boolean){
//                    Boolean boolVal = (Boolean)o;
//                    if(boolVal){
//                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                        Toast.makeText(getActivity(), "locked", Toast.LENGTH_SHORT).show();
//                    } else{
//                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
//                        Toast.makeText(getActivity(), "unlocked", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                return true;
//            }
//        });

    }
}
