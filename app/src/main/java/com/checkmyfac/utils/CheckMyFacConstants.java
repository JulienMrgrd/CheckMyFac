package com.checkmyfac.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CheckMyFacConstants {

    // Type de points
    public static final String TYPE_BU = "BU";
    public static final String TYPE_REST = "REST";
    public static final String TYPE_DISTR = "DISTR";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_BUS = "BUS";
    public static final String TYPE_METRO = "METRO";

    // Properties
    public static final String BIBLIOTHEQUE  = "BIBLIOTHEQUE";
    public static final String RESTAURANT  = "RESTAURANT";
    public static final String DISTRIBUTEUR  = "DISTRIBUTEUR";
    public static final String COULEUR_DEFAULT = "COULEUR_DEFAULT";

    // PREFERENCES
    public static final String SHARED_PREFERENCES = "SharedPreferences";
    public static final String PREF_HAS_CHANGED = "pref_has_changed";
    public static final String TYPE_MAP = "typeMap";
    public static final String CACHE_SYSTEM = "cacheSystem";
    public static final String CACHE_MAP = "cacheMap";
    public static final String CHANGE_FAC = "changeFac";
    public static final String THE_FAC = "TheFac";
    public static final String THE_FAC_INDEX = "TheFacIndex";
    public static final String PREF_MESSAGE  = "pref_msg";
    public static final String MSG  = "msg";
    static final String VERSION_CODE = "VERSION";


    // visibilité des markers
    public static final String PREF_TO_DISPLAY_BU = "toDisplay_pref_BU";
    public static final String PREF_TO_DISPLAY_DISTR = "toDisplay_pref_DISTR";
    public static final String PREF_TO_DISPLAY_REST = "toDisplay_pref_REST";
    public static final String PREF_TO_DISPLAY_USER = "toDisplay_pref_USER";
    public static final String PREF_TO_DISPLAY_METRO = "toDisplay_pref_METRO";
    public static final String PREF_TO_DISPLAY_BUS = "toDisplay_pref_BUS";

    // couleurs des marqueurs (voir paramètres)
    public static final String PREF_COLOR_DISTR = "COLOR_DISTR";
    public static final String PREF_COLOR_BU = "COLOR_BU";
    public static final String PREF_COLOR_REST = "COLOR_REST";
    public static final String PREF_COLOR_BUS = "COLOR_BUS";
    public static final String PREF_COLOR_METRO = "COLOR_METRO";

    public static final float DIST_PERIMETRE = (float) 0.03;

    // OTHER
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    static final String NUM_SPLITTER = "-";
    public static final float ALPHA_METRO = (float)0.9998;
    public static final float ALPHA_BUS = (float)0.9997;
    public static final String DISCRIMINANT_TRANSPORT = "tr";
    public static final String BUTTON_VISIBLE = "buttonVisible";
    public static final String SPINNER_VISIBLE = "spinnerVisible";

}
