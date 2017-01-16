package com.checkmyfac.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.checkmyfac.R;

public class HelpActivity  extends AppCompatActivity {       // Activite contenant un texte (F.A.Q), et action bar (titre, logo, bouton retour)

    private static final String TAG = "HelpActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate !");
        setContentView(R.layout.activity_help);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
