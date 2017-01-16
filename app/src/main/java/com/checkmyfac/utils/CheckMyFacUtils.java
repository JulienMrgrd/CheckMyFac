package com.checkmyfac.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.checkmyfac.BuildConfig;
import com.checkmyfac.R;
import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.checkmyfac.dao.transport.TransportDAO;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.checkmyfac.utils.AlgoUtils.bytes2KOString;
import static com.checkmyfac.utils.AlgoUtils.bytes2MOString;

public class CheckMyFacUtils {

    private static final String TAG = "CheckMyFacUtils";

    private CheckMyFacUtils() {}

    public static final int FORMAT_KO = 1;
    public static final int FORMAT_MO = 2;

    public static void updatePropertiesIfNecessary(PropertiesLoaderInterface loader,
                                                   SharedPreferences prefs, PointInteretDAO dao){
        if(hasBeenUpdated(prefs) || dao.isEmpty(false)) {
            dao.updateDBWithProperties(loader, prefs);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt(CheckMyFacConstants.VERSION_CODE, BuildConfig.VERSION_CODE);
            edit.apply();
            return;
        }
        Log.i(TAG, "Pas besoin d'insert from properties");
    }

    public static boolean hasBeenUpdated(SharedPreferences prefs){
        int versionCode = BuildConfig.VERSION_CODE;
        int versionCode_pref;
        boolean code = prefs.contains(CheckMyFacConstants.VERSION_CODE);
        if( !code ){
            versionCode_pref = versionCode;
        } else {
            versionCode_pref = prefs.getInt(CheckMyFacConstants.VERSION_CODE, versionCode);
        }

        return (versionCode != versionCode_pref) || !code;
    }

    /** Méthode permettant de vider la map stocké en cache */
    public static void clearCacheMap(Context context){

        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> Début de nettoyage des caches map <<<<<<<<<<<<<<<<<<<<");

        File ext = context.getExternalCacheDir();
        if(ext == null) return;
        long size_ext;
        if(ext.isDirectory()) {
            size_ext = folderSize(ext);
        }
        else size_ext = ext.length();
        Log.d(TAG, "size de l'external = " + size_ext + "bytes [ " + bytes2KOString(size_ext) + ' ' + bytes2MOString(size_ext) + " ])====");
        clearDir(ext, ext.getName());
        File extnew = context.getExternalCacheDir();
        long size_extnew;
        if(extnew.isDirectory()) {
            size_extnew = folderSize(extnew);
        }
        else size_extnew = extnew.length();
        Log.d(TAG, "size de l'external = " + size_extnew + "bytes [ " + bytes2KOString(size_extnew) + ' ' + bytes2MOString(size_extnew) + " ])====");

        // si retour != "" alors OK
        clearDir(ext, ext.getName());

        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> Fin du nettoyage des caches map <<<<<<<<<<<<<<<<<<<<");
    }

    /**
     * Cette fonction supprime les caches de l'application, tout en conservant la fac de l'utilisateur.
     */
    public static void clearApplicationData(Context context, SharedPreferences prefs) {
        String the_fac = prefs.getString(CheckMyFacConstants.THE_FAC, null);
        Log.d(TAG, ">>>>>> nettoyage des caches users <<<<<<<\n");

        File appDir = new File(context.getCacheDir().getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            String res;
            for(String str : children){
                if(!str.equals("lib") && !str.equals("files")) {
                    res = clearDir(new File(appDir, str), str);
                    if(res.isEmpty()) Log.d(TAG, str +" pas ete vide correctement");
                    else Log.d(TAG, str +" est d\u00E9sormais vide !");
                }
            }
        }
        Log.d(TAG, ">>>>>>>>>> Fin du nettoyage des caches users <<<<<<<<<");

        if(the_fac!=null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CheckMyFacConstants.THE_FAC, the_fac);
            editor.apply();
        }
    }

    /**
     * La fonction vide un dossier. Si celui-ci est correctement vid\u00E9, elle retourne son nom, une chaine vide sinon
     */
    private static String clearDir(File folder, String folderToNotDelete) {     // recursive
        boolean everyIsDelete = true;
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    everyIsDelete = !clearDir(f, folderToNotDelete).isEmpty(); // EveryIsDelete == true si la chaine est non vide
                } else {
                    everyIsDelete = f.delete();
                    Log.d(TAG, f.getName()+" est supprim\u00E9e (directory ? "+f.isDirectory()+')');
                }
            }
        }
        if( !folder.getName().equalsIgnoreCase(folderToNotDelete)) {
            everyIsDelete = folder.delete();
            Log.d(TAG, folder.getName()+" est supprim\u00E9e (directory ? "+folder.isDirectory()+')');
        }

        if(everyIsDelete) return folderToNotDelete;
        else return "";
    }

    /**
     * Retourne la taille des caches de la carte google, selon le format voulu
     */
    public static String getSizeOfCacheMap(Context context, int flagOctet){
        File ext = context.getExternalCacheDir();
        if(ext==null) return null;
        long size_ext;
        if(ext.isDirectory()) {
            size_ext = folderSize(ext);
        }
        else size_ext = ext.length();
        if(flagOctet==FORMAT_KO) return AlgoUtils.bytes2KOString(size_ext);
        else return AlgoUtils.bytes2MOString(size_ext);
    }

    /**
     * Retourne la taille des caches de la carte google, selon le format voulu
     */
    public static String getSizeOfCacheUser(Context context, int flagOctet){

        File appDir = new File(context.getCacheDir().getParent());
        long size_ext = 0;
        if(appDir.exists()){
            File[] childrens = appDir.listFiles();
            String str;
            for(File children : childrens){
                str=children.getName();
                if(!str.equals("lib") && !str.equals("files")) {
                    size_ext += folderSize(children);
                }
            }
            if(flagOctet==FORMAT_KO) return bytes2KOString(size_ext);
            else return AlgoUtils.bytes2MOString(size_ext);
        } else {
            return "0";
        }

    }

    /**
     * Methode servant uniquement pour les tests. Elle permet d'enregistrer sur la carte sd la DB (pour la visualiser plus tard
     * sur un logiciel lisant le SQLite)
     */
    private static void saveDBInSDCard() {
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                String currentDBPath = "data/com.checkmyfac/databases/pts_interets.db";
                String backupDBPath = "pts_interets.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    try(FileOutputStream fos = new FileOutputStream(currentDB);
                        FileOutputStream fosBack = new FileOutputStream(backupDB);
                        FileChannel src = fos.getChannel();
                        FileChannel dst = fosBack.getChannel()){
                        dst.transferFrom(src, 0, src.size());
                    }
                }

                currentDBPath = "data/com.checkmyfac/databases/transports.db";
                backupDBPath = "transports.db";
                currentDB = new File(data, currentDBPath);
                backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    if (currentDB.exists()) {
                        try(FileOutputStream fos = new FileOutputStream(currentDB);
                            FileOutputStream fosBack = new FileOutputStream(backupDB);
                            FileChannel src = fos.getChannel();
                            FileChannel dst = fosBack.getChannel()){
                            dst.transferFrom(src, 0, src.size());
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }
    }

    private static long folderSize(File directory) {
        if( ! directory.isDirectory() ) return directory.length();

        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static void changeFacCleaner(final Context context, final SharedPreferences prefs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(CheckMyFacConstants.THE_FAC);
                editor.remove(CheckMyFacConstants.THE_FAC_INDEX);
                editor.remove(CheckMyFacConstants.PREF_COLOR_BU);
                editor.remove(CheckMyFacConstants.PREF_COLOR_DISTR);
                editor.remove(CheckMyFacConstants.PREF_COLOR_REST);
                editor.remove(CheckMyFacConstants.PREF_COLOR_BUS);
                editor.remove(CheckMyFacConstants.PREF_COLOR_METRO);
                editor.remove(CheckMyFacConstants.BUTTON_VISIBLE);
                editor.apply();
                new PointInteretDAO(context).clearAll();
                new TransportDAO(context).clearAll();
            }
        }).start();
    }

    public static Integer[] splitInteger(String nums_db) {

        if(nums_db==null) return null;

        if(nums_db.contains(" ")) nums_db = nums_db.replaceAll(" ", "");
        if(nums_db.contains(CheckMyFacConstants.NUM_SPLITTER)){
            String[] nums_db_split = nums_db.split(CheckMyFacConstants.NUM_SPLITTER);
            Integer[] numeros = new Integer[nums_db_split.length];
            for(int i=0; i<nums_db_split.length; i++){
                numeros[i] = Integer.parseInt( nums_db_split[i] );
            }
            return numeros;
        } else {
            return new Integer[]{ Integer.parseInt(nums_db) };
        }
    }

    public static String[] splitArray(String nums_db) {
        if(nums_db==null) return null;

        nums_db = nums_db.replaceAll(" ", "");
        if(nums_db.contains(CheckMyFacConstants.NUM_SPLITTER)){
            return nums_db.split(CheckMyFacConstants.NUM_SPLITTER);
        } else {
            return new String[]{ nums_db };
        }
    }

    public static List<String> splitList(String nums_db) {
        if(nums_db==null) return null;
        return Arrays.asList(splitArray(nums_db));
    }

    public static String joinInteger(Integer[] numeros) {
        StringBuilder join_num = new StringBuilder();
        for(Integer num : numeros){
            join_num.append(num + CheckMyFacConstants.NUM_SPLITTER);
        }
        return join_num.substring(0, join_num.length() - CheckMyFacConstants.NUM_SPLITTER.length());
    }


    public static boolean isInPerimeter(Location locationUser, LatLng locationCenterUniversity){
        float distanceCenter = CheckMyFacConstants.DIST_PERIMETRE;
        return !(locationUser.getLatitude() > locationCenterUniversity.latitude + distanceCenter ||
                locationUser.getLatitude() < locationCenterUniversity.latitude - distanceCenter ||
                locationUser.getLongitude() > locationCenterUniversity.longitude + distanceCenter ||
                locationUser.getLongitude() < locationCenterUniversity.longitude - distanceCenter);
    }

    public static Date getDatePi(PointInteret pi){
        Date sqlDate = null;
        if(pi.getDate()!=null && pi.getDate().length()==8) {
            sqlDate = Date.valueOf(pi.getDate().subSequence(0, 4).toString()
                    + '-' + pi.getDate().subSequence(4, 6).toString()
                    + '-' + pi.getDate().subSequence(6, 8).toString());
        }
        return sqlDate;
    }

    public static List<String> getListNameFromListOfPi(List<PointInteret> listePi) {
        if(listePi!=null && !listePi.isEmpty()){
            List<String> listName = new ArrayList<>();
            for (PointInteret pointInteret : listePi) {
                listName.add(pointInteret.getName());
            }
            return listName;
        }
        return null;
    }

    public static boolean[] getArrayIsVisibleFromListOfPi(List<PointInteret> listePi) {
        if(listePi!=null && !listePi.isEmpty()){
            boolean[] arrayVisible = new boolean[listePi.size()];
            int i = 0;
            for (PointInteret pointInteret : listePi) {
                arrayVisible[i]=pointInteret.isVisible();
                i++;
            }
            return arrayVisible;
        }
        return null;
    }

    public static boolean isAPointType(String type) {
        return type.equals(CheckMyFacConstants.TYPE_USER) || type.equals(CheckMyFacConstants.TYPE_BU)
                || type.equals(CheckMyFacConstants.TYPE_DISTR) || type.equals(CheckMyFacConstants.TYPE_REST)
                || type.equals(CheckMyFacConstants.TYPE_METRO) || type.equals(CheckMyFacConstants.TYPE_BUS);
    }

    public static void setVisibilityByType(String type, boolean visibility, PointInteretDAO piDAO,
                                           SharedPreferences prefs){
        if(piDAO!=null && CheckMyFacUtils.isAPointType(type)){
            piDAO.updateAllVisibilityPILikeType(type, visibility);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getPrefConstantByType(type), visibility);
            editor.apply();
        }
    }

    public static String getPrefConstantBySwitchId(int id){
        return getPrefConstantByType(getTypeByBySwitchId(id));
    }

    public static String getPrefConstantByType(String type) {

        switch (type.toUpperCase()){
            case CheckMyFacConstants.TYPE_BU:
                return CheckMyFacConstants.PREF_TO_DISPLAY_BU;
            case CheckMyFacConstants.TYPE_REST:
                return CheckMyFacConstants.PREF_TO_DISPLAY_REST;
            case CheckMyFacConstants.TYPE_DISTR:
                return CheckMyFacConstants.PREF_TO_DISPLAY_DISTR;
            case CheckMyFacConstants.TYPE_BUS:
                return CheckMyFacConstants.PREF_TO_DISPLAY_BUS;
            case CheckMyFacConstants.TYPE_METRO:
                return CheckMyFacConstants.PREF_TO_DISPLAY_METRO;
            default:
                return null;
        }
    }

    public static String getTypeByBySwitchId(int id) {
        switch (id){
            case R.id.nav_bibliotheque: return CheckMyFacConstants.TYPE_BU;
            case R.id.nav_distributeur: return CheckMyFacConstants.TYPE_DISTR;
            case R.id.nav_restauration: return CheckMyFacConstants.TYPE_REST;
            case R.id.nav_user: return CheckMyFacConstants.TYPE_USER;
        }
        return null;
    }
}
