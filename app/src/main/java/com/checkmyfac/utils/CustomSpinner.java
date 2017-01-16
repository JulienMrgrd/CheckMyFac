package com.checkmyfac.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**Classe permettant que l'adapter soit simplement une liste avec un premier
 * element cache (qui sera utilise pour servir d'item selectionne constamment)
 */
public class CustomSpinner extends ArrayAdapter<String> {
    private Context context;
    private int itemACacher;

    public CustomSpinner(Context context, int textViewResourceId, List<String> objects, int itemACacher) {
        super(context, textViewResourceId, objects);
        this.context=context;
        this.itemACacher = itemACacher;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View vue;
        if (position == itemACacher) { // Astuce : on cache le premier element (exemple : "Votre université")
            TextView tv = new TextView(context);
            tv.setHeight(0);
            tv.setClickable(false);
            tv.setVisibility(View.INVISIBLE);
            vue = tv;
        } else {
            vue = super.getDropDownView(position, null, parent);
        }
        return vue;
    }

}