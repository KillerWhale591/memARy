package com.killerwhale.memary.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.killerwhale.memary.Preference;
import com.killerwhale.memary.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void onStop() {
        Preference.setPreferences(getBaseContext());
        super.onStop();
    }
}
