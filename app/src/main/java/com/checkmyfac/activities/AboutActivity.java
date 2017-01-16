package com.checkmyfac.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.checkmyfac.R;

public class AboutActivity  extends AppCompatActivity {      // Activité contenant un texte (Les "A propos"), et action bar (titre, logo, bouton retour)

    private static final String TAG = "AboutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate !");
        setContentView(R.layout.activity_about);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
