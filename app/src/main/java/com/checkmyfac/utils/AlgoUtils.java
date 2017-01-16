package com.checkmyfac.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public abstract class AlgoUtils {

    public static CharSequence[] listStringToCharSequenceArray(List<String> list) {
        if(list == null) return null;
        return list.toArray(new CharSequence[list.size()]);
    }

    static String bytes2KOString(long sizeInBytes) {
        double SPACE_KO = 1000;
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(3);

        try{
            return nf.format(sizeInBytes/SPACE_KO) + " Ko";
        } catch (Exception e) {
            return sizeInBytes + " Byte(s)";
        }

    }

    static String bytes2MOString(long sizeInBytes) {
        double SPACE_MO = 1000*1000;
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(3);

        try{
            return nf.format(sizeInBytes/SPACE_MO) + " Mo";
        } catch (Exception e) {
            return sizeInBytes + " Byte(s)";
        }

    }

}
