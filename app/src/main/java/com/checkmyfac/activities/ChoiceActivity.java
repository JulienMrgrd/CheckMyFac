package com.checkmyfac.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.checkmyfac.R;
import com.checkmyfac.properties.PropertiesLoader;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CustomSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ChoiceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ChoiceActivity";

    private SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate !");
        setContentView(R.layout.activity_choice);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        prefs = this.getSharedPreferences(CheckMyFacConstants.SHARED_PREFERENCES, MODE_PRIVATE);

        int facChoiceIndex = prefs.getInt(CheckMyFacConstants.THE_FAC_INDEX, 0);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerChoice);
        if (prefs.getBoolean(CheckMyFacConstants.SPINNER_VISIBLE, false)){
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(CheckMyFacConstants.SPINNER_VISIBLE, true);
            editor.apply();
        }

        if ( prefs.getBoolean(CheckMyFacConstants.BUTTON_VISIBLE, false) ){
            findViewById(R.id.validChoice).setVisibility(View.VISIBLE);
        }

        List<String> facs = new LinkedList<>(Arrays.asList(new PropertiesLoader(this).getAllFacNames()));
        facs.add(0, getString(R.string.univChoix));
        CustomSpinner dataAdapter = new CustomSpinner(this, android.R.layout.simple_spinner_dropdown_item, facs, 0);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
        if(facChoiceIndex!=0){
            spinner.setSelection(facChoiceIndex);
        }
    }

    @Override
    public void onBackPressed() {
        // On force la fermeture de l'application
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Méthode permettant d'écouter le choix de l'université de l'utilisateur
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if( position!=0 && (position != prefs.getInt(CheckMyFacConstants.THE_FAC_INDEX, 0)) ){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(CheckMyFacConstants.THE_FAC_INDEX, position);

            //Affichage du bouton uniquement quand une université est selectionée
            if(!prefs.getBoolean(CheckMyFacConstants.BUTTON_VISIBLE, false)) {
                findViewById(R.id.validChoice).setVisibility(View.VISIBLE);
                editor.putBoolean(CheckMyFacConstants.BUTTON_VISIBLE, true);
            }
            editor.apply();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Lancement de l'activité de MapActivity
     */
    public void startMapActivity(View view){

        if(prefs.getString(CheckMyFacConstants.THE_FAC, null)==null) {
            SharedPreferences.Editor editor = prefs.edit();
            Spinner spinner = (Spinner) findViewById(R.id.spinnerChoice);
            editor.putString(CheckMyFacConstants.THE_FAC, (String)spinner.getItemAtPosition(prefs.getInt(CheckMyFacConstants.THE_FAC_INDEX, 0)));
            editor.apply();
        }
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        this.finish();
    }

}
