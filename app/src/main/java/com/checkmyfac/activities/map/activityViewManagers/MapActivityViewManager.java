package com.checkmyfac.activities.map.activityViewManagers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.checkmyfac.R;
import com.checkmyfac.activities.MapActivity;
import com.checkmyfac.activities.map.mapState.MapState;
import com.checkmyfac.activities.map.tasks.AsyncTransportTask;
import com.checkmyfac.activities.map.transport.TransportKeys;
import com.checkmyfac.interfaces.OnTransportsCallback;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.properties.PropertiesLoader;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;
import com.checkmyfac.utils.ColorUtils.ColorConstants;
import com.checkmyfac.utils.ColorUtils.ColorUtils;
import com.checkmyfac.utils.FabUtils;
import com.checkmyfac.utils.MyExpandableListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.List;

/**
 *  Classe d'initialisation des éléments graphiques de MapActivity
 *  (permet de séparer le code propre aux vues du code métier)
 */
public class MapActivityViewManager {

    private static final String TAG = "MapActivityViewManager";
    private MapActivity context;
    private ImageView add;
    private ImageView modify;
    private ImageView locate;

    public MapActivityViewManager(Context context){
        if(context != null && !(context instanceof MapActivity) ){
            throw new IllegalArgumentException("Context is not a MapActivity");
        }
        this.context = ((MapActivity) context);
    }

    /**
     * Retourne le nouveau marker, null sinon.
     * @param mMap la GoogleMap
     * @param pi le nouveau point
     * @param oldMarker l'ancien, null si c'est un nouvel ajout
     * @return le Marker ajouté à la map
     */
    public Marker addOrUpdateMarkerUserOnMap(GoogleMap mMap, PointInteret pi, Marker oldMarker){

        LatLng pos = new LatLng(pi.getLat(), pi.getLon()); // Position du PI
        String type = pi.getType();
        String couleur = pi.getCoul();
        // Couleur suivant choix
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(ColorUtils.getIdBitmapByTypeAndColor(type,couleur));

        if(pi.getDesc() == null && pi.getName() == null) return null;
        if(oldMarker == null) { // new Marker
            return mMap.addMarker(new MarkerOptions()
                    .title(pi.getName())
                    .snippet(pi.getDesc())
                    .position(pos)
                    .icon(bitmap)
                    .draggable(true));

        }else{ // MAJ du point
            if(!pi.getName().isEmpty()) oldMarker.setTitle(pi.getName());
            oldMarker.setSnippet(pi.getDesc());
            oldMarker.setPosition(pos);
            oldMarker.setIcon(bitmap);
            oldMarker.showInfoWindow();
        }
        return null;
    }

    /**
     * Méthode créant le FloatingActionMenu et l'attacher au FloatingActionButton
     * @param fab le FloatingActionButton
     * @return FloatingActionMenu à attacher au FloatingActionButton
     * @throws IllegalArgumentException
     */
    public FloatingActionMenu initFAB(final FloatingActionButton fab)
            throws IllegalArgumentException {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;

        // With custom button and content sizes and margins
        //Adaptation de la taille du bouton en fonction de la taille de l'ecran
        int blueSubActionButtonSize = heightPixels/14;

        // Set up customized SubActionButtons for the right center menu
        final SubActionButton.Builder builder = new SubActionButton.Builder(context);
        builder.setBackgroundDrawable(context.getDrawable(R.drawable.blue_light_fab_selector));

        FrameLayout.LayoutParams blueParams = new FrameLayout.LayoutParams(blueSubActionButtonSize,
                blueSubActionButtonSize);
        builder.setLayoutParams(blueParams);

        SubActionButton.Builder builderdark = new SubActionButton.Builder(context);
        builderdark.setBackgroundDrawable(context.getDrawable(R.drawable.blue_dark_fab_selector));

        builderdark.setLayoutParams(blueParams);

        // Set custom layout params
        add = new ImageView(context);
        add.setImageResource(R.drawable.ic_add_light_24dp);

        SubActionButton addSubActionViewAdd = builder.setContentView(add).build();
        addSubActionViewAdd.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                toastMessage(context.getString(R.string.ClickOnMap));
                context.changeMapState(MapState.NEW_MARKER);
            }
        });

        modify = new ImageView(context);
        modify.setImageResource(R.drawable.ic_create_light_24dp);

        SubActionButton addSubActionViewModify = builderdark.setContentView(modify).build();
        addSubActionViewModify.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                toastMessage(context.getString(R.string.SelectPoint));
                context.changeMapState(MapState.UPDATE_OR_DELETE_MARKER);
            }
        });

        locate = new ImageView(context);
        locate.setImageResource(R.drawable.ic_gps_fixed_light_24dp);

        SubActionButton addSubActionViewLocate = builder.setContentView(locate).build();
        addSubActionViewLocate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                context.centerMapIfInPerimeter();
            }
        });

        // Build another menu with custom options
        FloatingActionMenu menu = new FloatingActionMenu.Builder(context)
                .addSubActionView(addSubActionViewLocate)
                .addSubActionView(addSubActionViewModify)
                .addSubActionView(addSubActionViewAdd) // the most above
                .attachTo(fab)
                .build();

        menu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                FabUtils.animateFab(context, fab, FabUtils.OPEN_FAB);
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                FabUtils.animateFab(context, fab, FabUtils.CLOSE_FAB);
            }
        });

        return menu;
    }

    /**
     * Initialisation de la GoogleMap
     * @param context
     * @param fileLoader fichier properties permettant d'initialiser la map
     * @param map la GoogleMap à initialiser
     * @throws IllegalArgumentException
     */
    public void initMap(final Context context, final PropertiesLoaderInterface fileLoader, final GoogleMap map)
            throws IllegalArgumentException {

        LatLng fac = new LatLng(fileLoader.getLatFac(),fileLoader.getLonFac()); // position du centre de Jussieu
        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(fac)
                        .zoom(fileLoader.getZoom())
                        .bearing(fileLoader.getRotation())
                        .build()));   //Changement de l'angle

        // Recupere les preferences associees aux parametres
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String mapType = prefs.getString(CheckMyFacConstants.TYPE_MAP, Integer.toString(GoogleMap.MAP_TYPE_NORMAL));  // Par defaut: GoogleMap.MAP_TYPE_HYBRID (constante egale a 4)
        setMapType(map, mapType);

        map.getUiSettings().setZoomControlsEnabled(false);   // Bouton zoom
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

        map.setMyLocationEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);  // Cache le bouton "localisation"
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        map.setIndoorEnabled(false);
        // Ici on configure le visuel des fenetres d'informations des markers
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                LinearLayout layout = new LinearLayout(context);

                TextView tv_titre = new TextView(context);
                tv_titre.setText(marker.getTitle());
                tv_titre.setGravity(Gravity.CENTER);
                tv_titre.setTypeface(Typeface.DEFAULT_BOLD);

                if(marker.getSnippet()==null || marker.getSnippet().isEmpty()){ // description
                    layout.addView(tv_titre);
                    layout.setBackgroundResource(R.drawable.custom_info_window);
                    return layout;
                }

                TextView tv_snippet = new TextView(context);
                tv_snippet.setText(marker.getSnippet());
                tv_snippet.setTextColor(Color.GRAY);

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(tv_titre);
                layout.addView(tv_snippet);
                layout.setBackgroundResource(R.drawable.custom_info_window);
                return layout;
            }

            @Override
            public View getInfoContents(Marker marker) {  // Si pas de snippet, alors infoWindow plus petite, sinon snippet avec multiline
                return null;
            }
        });
    }

    /**
     * Méthode modifiant le type de la map
     * @param map la GoogleMap à modifier
     * @param mapType nouveau de type à appliquer à la map
     */
    public void setMapType(GoogleMap map, String mapType){
        Log.w("MapActivity", "mapType =="+mapType);
        if(map!=null){
            try {
                map.setMapType(Integer.parseInt(mapType));
            } catch (NumberFormatException ignored){
                Log.w(TAG, "setMapType, type not a int : "+mapType);
            }
        }
    }

    /**
     * Centrage de la map sur la position du marker
     * @param mMap la GoogleMap à modifier
     * @param marker marker sur lequel il faut centrer la GoogleMAp
     */
    public void moveCameraOnMarker(GoogleMap mMap, Marker marker) {
        if(mMap!=null && marker!=null){
            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),
                    mMap.getCameraPosition().zoom));
        }
    }

    public MyExpandableListAdapter displayTransportDialog(final OnTransportsCallback callback,
                                                          final TransportKeys type,
                                                          final String titleDialog,
                                                          final String titleTransport,
                                                          final List<String> headers) {

        ExpandableListView myList = new ExpandableListView(context);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(context, headers);
        myList.setAdapter(adapter);
        myList.setPadding(30, 30, 30, 30);
        myList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                AsyncTransportTask task = new AsyncTransportTask(context, type, titleDialog,
                        headers.get(groupPosition), titleTransport);
                task.setHttpListener(callback);
                task.execute();
            }
        });

        AlertDialog.Builder confirmation = new AlertDialog.Builder(context);
        confirmation.setTitle(titleDialog)
                .setCancelable(true)
                .setView(myList);
        confirmation.create();
        confirmation.show();
        return adapter;
    }

    /**
     * Méthode permettant de fermer ou ouvrir le FloatingActionMenu du FloatingActionButton
     * @param toOpen vrai pour l'ouvrir et faux pour le fermer
     * @param fab FloatingActionButton
     * @param menu FloatingActionMenu
     */
    public void animateFAB(boolean toOpen, FloatingActionButton fab, FloatingActionMenu menu) {
        if(fab ==null || menu==null) return;
        if(toOpen && !menu.isOpen()){
            FabUtils.animateFab(context, fab, FabUtils.OPEN_FAB);
            menu.open(true);
        } else if(!toOpen && menu.isOpen()){
            FabUtils.animateFab(context, fab, FabUtils.CLOSE_FAB);
            menu.close(true);
        }
    }

    /**
     * Centrage de la map sur la position de la localisation si elle est dans le périmétre
     * @param mMap la GoogleMap
     * @param location la localisation où centrer la map
     */
    public void moveMapOnPositionIfInPerimeter(GoogleMap mMap, Location location){
        if(mMap!=null && location!=null){
            PropertiesLoaderInterface fileLoader = new PropertiesLoader(context);
            LatLng positionUniversity = new LatLng(fileLoader.getLatFac(), fileLoader.getLonFac());
            if(CheckMyFacUtils.isInPerimeter(location, positionUniversity)) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(fileLoader.getZoom())
                        .bearing(fileLoader.getRotation())
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }else{
                toastMessage(context.getString(R.string.HorsPerim));
            }
        }else{
            toastMessage(context.getString(R.string.LocImpossible));
        }
    }

    /**
     * Création du spinner qui apparait dans les alertDialog de la creation et la modification d'un point d'interet
     * pour la sélection de la couleur de celui-ci
     * @param dialog AlertDialog dans lequel il faut ajouter le spinner
     * @return le Spinner
     */
    public Spinner initSpinnerCouleur(AlertDialog dialog) {
        if(dialog==null) return null;
        String[] couleurs = ColorConstants.LIST;
        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, couleurs);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        Spinner spinner_couleur = (Spinner) dialog.findViewById(R.id.spinner_create_marker_dialog);
        spinner_couleur.setAdapter(adapter);
        return spinner_couleur;
    }

    /**
     * Modification des subFloatingActionButton en fonction des états de la map
     * @param state état de la map
     */
    public void setImageDependingState(MapState state){
        if(state.equals(MapState.NONE)){
            add.setImageResource(R.drawable.ic_add_light_24dp);
            modify.setImageResource(R.drawable.ic_create_light_24dp);
        } else if(state.equals(MapState.NEW_MARKER)){
            add.setImageResource(R.drawable.ic_add_black_24dp);
            modify.setImageResource(R.drawable.ic_create_light_24dp);
        }else if(state.equals(MapState.UPDATE_OR_DELETE_MARKER)){
            add.setImageResource(R.drawable.ic_add_light_24dp);
            modify.setImageResource(R.drawable.ic_create_black_24dp);
        }
    }

    /**
     * Methode permettant d'afficher les messages toast si la préférence est cochée
     * @param message le message à afficher
     */
    public void toastMessage(String message){
        if(context.getPrefs().getBoolean(CheckMyFacConstants.PREF_MESSAGE,true)){
            Toast.makeText(context, message,Toast.LENGTH_LONG).show();
        }
    }
}
