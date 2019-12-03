package com.killerwhale.memary.View;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.killerwhale.memary.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
    }
}
