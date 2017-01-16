package com.checkmyfac.activities.map.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.checkmyfac.dao.transport.TransportDAO;
import com.checkmyfac.interfaces.OnMarkersReadyCallback;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.objet.Transport;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;
import com.checkmyfac.utils.ColorUtils.ColorUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//////////////////////
/// AsyncTask : Charge en tâche de fond l'ensemble des données contenues dans la base
/////////////////////
public class AsyncAddMarker extends AsyncTask<Void, Void, Integer>{

    private static final String TAG = "AsyncAddMarker";
    private GoogleMap map;
    private Context context;
    private SharedPreferences prefs;
    private PropertiesLoaderInterface loader;
    private PointInteretDAO ptsInteretsDAO;
    private TransportDAO transportDAO;

    private Map<String, MarkerOptions> mapIdMarkerOptionsPI;
    private Map<String, MarkerOptions> mapIdMarkerOptionsTransports;

    public AsyncAddMarker(Context context, GoogleMap map, PointInteretDAO ptsInteretsDAO,
                          TransportDAO transportDAO, SharedPreferences prefs,
                          PropertiesLoaderInterface loader){
        this.map=map;
        this.context=context;
        this.ptsInteretsDAO = ptsInteretsDAO;
        this.transportDAO=transportDAO;
        this.prefs=prefs;
        this.loader = loader;

        mapIdMarkerOptionsPI = new HashMap<>();
        mapIdMarkerOptionsTransports = new HashMap<>();
    }

    /**
     * Récuperation de l'ensemble des points d'intérêts et des transports en arrière plan
     * et insertion de ceux-ci dans les HashMap correspondantes
     */
    public Integer doInBackground(Void...params) {     // Insertion des PI par defaults (en fond)
        Log.d(TAG, "doInBackground");

        // Bool modifies par l'utilisateur (filtres) - permettent l'affichage ou pas de ces points
        boolean boolBU = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_BU, true); // points à afficher ou non
        boolean boolDistrib = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_DISTR, true);
        boolean boolRest = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_REST, true);
        boolean boolUser = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_USER, true);
        boolean boolBus = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_BUS, true);
        boolean boolMetro = prefs.getBoolean(CheckMyFacConstants.PREF_TO_DISPLAY_METRO, true);

        // Si base vide ou app mise à jour
        CheckMyFacUtils.updatePropertiesIfNecessary(loader, prefs, ptsInteretsDAO);

        if(!ptsInteretsDAO.isEmpty(false)){
            ptsInteretsDAO.deletePIExpirate();
        }
        List<PointInteret> list_PI = ptsInteretsDAO.getAll();
        List<Transport> list_transport = transportDAO.getAll();

        LatLng pos;
        String type;
        BitmapDescriptor bitmap;
        MarkerOptions marker_tmp = null;

        for(PointInteret pi : list_PI) {    // ArrayList de tous les PI par default

            pos=new LatLng(pi.getLat(), pi.getLon()); // Position du PI
            type = pi.getType();
            bitmap = BitmapDescriptorFactory.fromResource(ColorUtils.getIdBitmapByTypeAndColor(type,pi.getCoul()));

            if(type.equalsIgnoreCase(CheckMyFacConstants.TYPE_DISTR) && boolDistrib){
                marker_tmp = getMarkerOptions(pi.getName(), pi.getDesc(), pos, bitmap, null, false, pi.isVisible());

            } else if (type.equalsIgnoreCase(CheckMyFacConstants.TYPE_BU) && boolBU){
                marker_tmp = getMarkerOptions(pi.getName(), pi.getDesc(), pos, bitmap, null, false, pi.isVisible());

            } else if (type.equalsIgnoreCase(CheckMyFacConstants.TYPE_REST) && boolRest){
                marker_tmp = getMarkerOptions(pi.getName(), pi.getDesc(), pos, bitmap, null, false, pi.isVisible());

            } else if (boolUser){
                marker_tmp = getMarkerOptions(pi.getName(), pi.getDesc(), pos, bitmap, null, true, pi.isVisible());
            }

            if(marker_tmp != null) mapIdMarkerOptionsPI.put(pi.getId(), marker_tmp);
        }

        for(Transport transport : list_transport) {    // ArrayList de tous les PI par default

            pos = new LatLng(transport.getLat(), transport.getLon()); // Position du PI
            type = transport.getType();
            bitmap = BitmapDescriptorFactory.fromResource(ColorUtils.getIdBitmapByTypeAndColor(type, transport.getCouleur()));
            if (type.contains(CheckMyFacConstants.TYPE_BUS) && boolBus) {
                marker_tmp = getMarkerOptions(transport.getDescription(), transport.getNumero(), pos, bitmap, CheckMyFacConstants.ALPHA_BUS, false, true);

            } else if (type.contains(CheckMyFacConstants.TYPE_METRO) && boolMetro) {
                marker_tmp = getMarkerOptions(transport.getDescription(), transport.getNumero(), pos, bitmap, CheckMyFacConstants.ALPHA_METRO, false, true);
            }
            if(marker_tmp != null) mapIdMarkerOptionsTransports.put(transport.getId(), marker_tmp);
        }

        return 1;
    }

    public void onPostExecute(Integer param){
        Log.d(TAG, "ajout sur map");
        map.clear();

        Map<String, Marker> markers = new HashMap<>();
        if(mapIdMarkerOptionsPI != null){
            for(Map.Entry<String, MarkerOptions> entry : mapIdMarkerOptionsPI.entrySet()){
                if(entry != null) markers.put(entry.getKey(), putMarkerOnMap(map, entry.getValue()));
            }
        }
        if(mapIdMarkerOptionsTransports != null){
            for(Map.Entry<String, MarkerOptions> entry : mapIdMarkerOptionsTransports.entrySet()){
                if(entry != null) markers.put(entry.getKey(), putMarkerOnMap(map, entry.getValue()));
            }
        }

        ((OnMarkersReadyCallback) context).onTaskCompleted(markers); // Envoie la liste des markers a MapActivity
    }

    /**
     * Ajout d'un marker sur la map
     * @param map GoogleMap sur laquelle il faut ajouter le marker
     * @param marker marker à ajouter
     * @return le Marker créé
     */
    private static Marker putMarkerOnMap(GoogleMap map, MarkerOptions marker){
        if( marker == null ) return null;
        return map.addMarker(marker);
    }

    /**
     * Création d'un MarkerOptions en fonction des paramètres passés en arguments
     * @param title titre du marker
     * @param desc description du marker
     * @param pos coordonnée du marker
     * @param bitmap image du marker
     * @param alpha degré d'opacité du marker
     * @param draggable définit si le marker est déplaçable ou non
     * @param visible définit si le marker est visible ou non
     * @return le MarkerOption créé
     */
    private static MarkerOptions getMarkerOptions(String title, String desc, LatLng pos, BitmapDescriptor bitmap,
                                           Float alpha, boolean draggable, boolean visible){
        if( (title == null && desc == null) || pos == null) return null;
        if(alpha==null) alpha = (float) 1;
        return new MarkerOptions()
                .title(title)
                .snippet(desc)
                .position(pos)
                .icon(bitmap)
                .draggable(draggable)
                .alpha(alpha)
                .visible(visible);
    }

    private static MarkerOptions getMarkerOptions(String title, Integer[] desc, LatLng pos, BitmapDescriptor bitmap,
                                           Float alpha, boolean draggable, boolean visible){
        if(desc != null){
            return getMarkerOptions(title, CheckMyFacUtils.joinInteger(desc), pos, bitmap, alpha, draggable, visible);
        }
        return null;
    }

}   // Fin asynctask