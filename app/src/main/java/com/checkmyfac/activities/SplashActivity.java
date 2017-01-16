package com.checkmyfac.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.checkmyfac.utils.CheckMyFacConstants;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = this.getSharedPreferences(CheckMyFacConstants.SHARED_PREFERENCES, MODE_PRIVATE); // Les préferences enregistrées

        if ( prefs.getString(CheckMyFacConstants.THE_FAC, "null").equals("null") ) {
            Intent intent = new Intent(this, ChoiceActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            finish();
        }
    }

}

