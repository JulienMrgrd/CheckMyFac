package com.checkmyfac.utils.ColorUtils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.checkmyfac.R;
import com.checkmyfac.utils.CheckMyFacConstants;

import java.util.Arrays;

public class ColorUtils {

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {  // Marker normal : 88-66

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight); // resize the bit map
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    public static int getIcon(String couleur, String type){
        type=type.toUpperCase();
        // Verification a faire lors de l'affichage des icones des List
        if( Arrays.asList(ColorConstants.LIST).contains(couleur) ) return getIdBitmapByTypeAndColor(type, couleur);
        else if(type.equals(CheckMyFacConstants.TYPE_DISTR)) return R.drawable.marker_distr_bleu;
        else if (type.equals(CheckMyFacConstants.TYPE_BU)) return R.drawable.marker_bu_orange;
        else if (type.equals(CheckMyFacConstants.TYPE_REST)) return R.drawable.marker_rest_rouge;
        else return R.drawable.marker_user_bleuciel;
    }

    public static int getIconTransports(String couleur, String type){
        type=type.toUpperCase();
        if( Arrays.asList(ColorConstants.LIST_TRANSPORTS).contains(couleur) ){  // Verification a faire lors de l'affichage des icones des List
            return getIdBitmapByTypeAndColor(type, couleur);
        } else {
            if(type.equals(CheckMyFacConstants.TYPE_BUS)) return R.drawable.bus_bleu_paon;
            else return R.drawable.metro_bleu_fonce;
        }
    }

    public static int getUserBitmapByColor(String couleur){
        switch (couleur) {

            case ColorConstants.BLEU:
                return R.drawable.marker_user_bleu;

            case ColorConstants.BLEU_CIEL:
                return R.drawable.marker_user_bleuciel;

            case ColorConstants.JAUNE:
                return R.drawable.marker_user_jaune;

            case ColorConstants.MARRON:
                return R.drawable.marker_user_marron;

            case ColorConstants.ORANGE:
                return R.drawable.marker_user_orange;

            case ColorConstants.ROUGE:
                return R.drawable.marker_user_rouge;

            case ColorConstants.VERT:
                return R.drawable.marker_user_vert;

            case ColorConstants.VIOLET:
                return R.drawable.marker_user_violet;

            default:
                return R.drawable.marker_user_bleuciel;
        }
    }



    public static int getIdBitmapByTypeAndColor(String type, String couleur){
        type = type.toUpperCase();
        switch (couleur) {
            case ColorConstants.BLEU:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_bleu;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_bleu;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_bleu;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_bleu;
                }

            case ColorConstants.BLEU_CIEL:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_bleuciel;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_bleuciel;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_bleuciel;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_bleuciel;
                }

            case ColorConstants.JAUNE:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_jaune;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_jaune;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_jaune;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_jaune;
                }

            case ColorConstants.MARRON:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_marron;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_marron;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_marron;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_marron;
                }

            case ColorConstants.ORANGE:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_orange;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_orange;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_orange;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_orange;
                }

            case ColorConstants.ROUGE:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_rouge;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_rouge;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_rouge;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_rouge;
                }

            case ColorConstants.VERT:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_vert;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_vert;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_vert;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_vert;
                }

            case ColorConstants.VIOLET:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_violet;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_violet;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_violet;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_violet;
                }

            case ColorConstants.PRUNE:
                switch (type) {
                    case CheckMyFacConstants.TYPE_BUS:
                        return R.drawable.bus_prune;
                    case CheckMyFacConstants.TYPE_METRO:
                        return R.drawable.metro_prune;
                }

            case ColorConstants.BLEU_FONCE:
                switch (type) {
                    case CheckMyFacConstants.TYPE_BUS:
                        return R.drawable.bus_bleu_fonce;
                    case CheckMyFacConstants.TYPE_METRO:
                        return R.drawable.metro_bleu_fonce;
                }

            case ColorConstants.BLEU_PAON:
                switch (type) {
                    case CheckMyFacConstants.TYPE_BUS:
                        return R.drawable.bus_bleu_paon;
                    case CheckMyFacConstants.TYPE_METRO:
                        return R.drawable.metro_bleu_paon;
                }

            case ColorConstants.GRIS:
                switch (type) {
                    case CheckMyFacConstants.TYPE_BUS:
                        return R.drawable.bus_gris;
                    case CheckMyFacConstants.TYPE_METRO:
                        return R.drawable.metro_gris;
                }

            default:
                switch (type) {
                    case CheckMyFacConstants.TYPE_DISTR:
                        return R.drawable.marker_distr_bleu;
                    case CheckMyFacConstants.TYPE_BU:
                        return R.drawable.marker_bu_orange;
                    case CheckMyFacConstants.TYPE_REST:
                        return R.drawable.marker_rest_rouge;
                    case CheckMyFacConstants.TYPE_USER:
                        return R.drawable.marker_user_bleuciel;
                    case CheckMyFacConstants.TYPE_METRO:
                        return R.drawable.metro_bleu_fonce;
                    case CheckMyFacConstants.TYPE_BUS:
                        return R.drawable.bus_bleu_paon;
                }
        }
        return R.drawable.marker_user_bleu; // Au cas où la couleur n'existerais pas (cas quasiment improbable)
    }


    public static String getPrefColorByType(String type) {

        switch (type.toUpperCase()){

            case CheckMyFacConstants.TYPE_BU:
                return CheckMyFacConstants.PREF_COLOR_BU;

            case CheckMyFacConstants.TYPE_REST:
                return CheckMyFacConstants.PREF_COLOR_REST;

            case CheckMyFacConstants.TYPE_DISTR:
                return CheckMyFacConstants.PREF_COLOR_DISTR;

            case CheckMyFacConstants.TYPE_BUS:
                return CheckMyFacConstants.PREF_COLOR_BUS;

            case CheckMyFacConstants.TYPE_METRO:
                return CheckMyFacConstants.PREF_COLOR_METRO;

            default:
                return null;
        }
    }

    public static String getTypeByPrefColor(String pref_color) {

        switch (pref_color.toUpperCase()){

            case CheckMyFacConstants.PREF_COLOR_BU:
                return CheckMyFacConstants.TYPE_BU;

            case CheckMyFacConstants.PREF_COLOR_REST:
                return CheckMyFacConstants.TYPE_REST;

            case CheckMyFacConstants.PREF_COLOR_DISTR:
                return CheckMyFacConstants.TYPE_DISTR;

            case CheckMyFacConstants.PREF_COLOR_BUS :
                return CheckMyFacConstants.TYPE_BUS;

            case CheckMyFacConstants.PREF_COLOR_METRO :
                return CheckMyFacConstants.TYPE_METRO;

            default:
                return null;
        }
    }
}
