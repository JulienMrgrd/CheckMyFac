package com.checkmyfac.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import com.checkmyfac.R;
import com.checkmyfac.activities.map.activityViewManagers.MapActivityViewManager;
import com.checkmyfac.activities.map.mapState.MapState;
import com.checkmyfac.activities.map.tasks.AsyncAddMarker;
import com.checkmyfac.activities.map.tasks.HoraireTransport;
import com.checkmyfac.activities.map.transport.TransportKeys;
import com.checkmyfac.dao.DAO;
import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.checkmyfac.dao.transport.TransportDAO;
import com.checkmyfac.interfaces.OnMarkersReadyCallback;
import com.checkmyfac.interfaces.OnTransportsCallback;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.properties.PropertiesLoader;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.checkmyfac.utils.AlgoUtils;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;
import com.checkmyfac.utils.ColorUtils.ColorUtils;
import com.checkmyfac.utils.MarkerUtils;
import com.checkmyfac.utils.MyExpandableListAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, OnMarkersReadyCallback, DrawerLayout.DrawerListener,
        OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener,
        OnInfoWindowClickListener, OnMarkerDragListener, CompoundButton.OnCheckedChangeListener,
        CalendarView.OnDateChangeListener, DialogInterface.OnDismissListener, OnTransportsCallback {

    private static final String TAG = "MapActivity";
    private static final int NB_CHECKED_DRAWER = 4; // voir activity_main_drawer

    private GoogleMap mMap;
    private PropertiesLoaderInterface fileLoader;
    private Map<String, Marker> idWithMarkers;
    private DAO pointInteretDAO, transportDAO;
    private SharedPreferences prefs;
    private MapState state;

    // Délégation de code (moins dans l'Activity, proche d'un pattern MVC)
    private MapActivityViewManager viewManager;
    private MarkerUtils markerUtils;

    private DrawerLayout drawer;
    private NavigationView nav;
    private FloatingActionButton fab;
    private FloatingActionMenu menu;

    private MyExpandableListAdapter horairesAdapter;
    private String dateExpiration;
    private LinkedHashMap<String, List<HoraireTransport>> transportWithHoraires;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "Dans onCreate");

        try { // Fluidifie le chargement de la map
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.mapView);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.getMapAsync(MapActivity.this); // will init map when ready
                        }
                    });
                }
            }).start();
        } catch (Exception e){
            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapView);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapFragment.getMapAsync(MapActivity.this); // will init map when ready
                }
            });
        }

        state = MapState.NONE;
        pointInteretDAO = new PointInteretDAO(this);
        transportDAO = new TransportDAO(this);
        fileLoader = new PropertiesLoader(this);
        prefs = this.getSharedPreferences(CheckMyFacConstants.SHARED_PREFERENCES, MODE_PRIVATE); // Recuperation des SharedPreferences "preference" (different des preferences de parametres)

        viewManager = new MapActivityViewManager(this);
        markerUtils = new MarkerUtils(this, idWithMarkers);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        menu = viewManager.initFAB(fab);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);
        nav = ((NavigationView) findViewById(R.id.nav_view));
        nav.setNavigationItemSelectedListener(this);
        setOnCheckedChangeListener(nav.getMenu());

        dateExpiration = CheckMyFacConstants.sdf.format(new Date());
    }

    @Override
    public void onBackPressed() {
        Log.d("CheckMyFac", "onBackPressed");
        animateFab(false);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        viewManager.initMap(this, fileLoader, mMap);
        new AsyncAddMarker(this, mMap, (PointInteretDAO) pointInteretDAO,
                (TransportDAO) transportDAO, prefs, fileLoader).execute();
        //Initialisation des listener de la map
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setMyLocationEnabled(true);

        //Changement de l'icone hamburger en fonction du type de la map
        ImageView hamburger = (ImageView) findViewById(R.id.hamburger);
        if(mMap!=null && mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID){
            if(hamburger!=null) hamburger.setImageResource(R.drawable.hamburger_icon_white);
        }else{
            if(hamburger!=null) hamburger.setImageResource(R.drawable.hamburger_icon);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // application des nouveaux paramètres
        if(prefs != null && prefs.contains(CheckMyFacConstants.PREF_HAS_CHANGED)) {
            if (viewManager == null && mMap == null){
                onCreate(null);
                return;
            }
            Set<String> newPrefsToApply = prefs.getStringSet(CheckMyFacConstants.PREF_HAS_CHANGED, new HashSet<String>(0));
            Log.d(TAG, "nouvelles preferences : " + newPrefsToApply);
            for (String pref : newPrefsToApply) {
                if (pref != null && pref.equals(CheckMyFacConstants.TYPE_MAP)) {
                    viewManager.setMapType(mMap, prefs.getString(CheckMyFacConstants.TYPE_MAP,
                            Integer.toString(GoogleMap.MAP_TYPE_NORMAL)));
                    ImageView hamburger = (ImageView) findViewById(R.id.hamburger);
                    if (mMap != null && mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID){
                        if(hamburger!=null) hamburger.setImageResource(R.drawable.hamburger_icon_white);
                    }else{
                        if(hamburger!=null) hamburger.setImageResource(R.drawable.hamburger_icon);
                    }
                } else if(CheckMyFacUtils.isAPointType(pref)) {
                    String colorByType = ColorUtils.getPrefColorByType(pref);
                    int icon = ColorUtils.getIdBitmapByTypeAndColor(pref, prefs.getString(colorByType, ""));
                    markerUtils.setMarkersIcon(markerUtils.getMarkersByType((PointInteretDAO) pointInteretDAO, pref),
                            BitmapDescriptorFactory.fromResource(icon));
                }
            }
            prefs.edit().remove(CheckMyFacConstants.PREF_HAS_CHANGED).apply();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();
        Intent nextActivity = null;
        List<PointInteret> listePi = null;

        if (id == R.id.nav_bibliotheque) {
            listePi=((PointInteretDAO) pointInteretDAO).getAllPiByType(CheckMyFacConstants.TYPE_BU);
        } else if (id == R.id.nav_restauration) {
            listePi=((PointInteretDAO) pointInteretDAO).getAllPiByType(CheckMyFacConstants.TYPE_REST);
        } else if (id == R.id.nav_distributeur) {
            listePi=((PointInteretDAO) pointInteretDAO).getAllPiByType(CheckMyFacConstants.TYPE_DISTR);
        } else if (id == R.id.nav_user) {
            listePi=((PointInteretDAO) pointInteretDAO).getAllPiByType(CheckMyFacConstants.TYPE_USER);
        } else if (id == R.id.nav_pref) {
            nextActivity = new Intent(this, MyPreferenceActivity.class);
        } else if (id == R.id.nav_help) {
            nextActivity = new Intent(this, HelpActivity.class);
        } else if (id == R.id.nav_about) {
            nextActivity = new Intent(this, AboutActivity.class);
        }
        if(nextActivity!=null){
            startActivity(nextActivity);
        } else {
            List<String> pi_list = CheckMyFacUtils.getListNameFromListOfPi(listePi);
            if (pi_list != null) {
                viewManager.toastMessage(this.getString(R.string.first_DialogLongClick));

                // affichage de la liste + gestion long click (centrage de la map sur le point sélectionnée)
                CharSequence[] pi_char_list = AlgoUtils.listStringToCharSequenceArray(pi_list);
                boolean[] arrayOfVisibility = CheckMyFacUtils.getArrayIsVisibleFromListOfPi(listePi);
                final ArrayList<Integer> mSelectedItems = new ArrayList<>(pi_list.size());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(item.getTitle())
                        .setMultiChoiceItems(pi_char_list, arrayOfVisibility,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which,
                                                        boolean isChecked) {
                                        if (isChecked) {
                                            mSelectedItems.add(which);
                                        } else if (mSelectedItems.contains(which)) {
                                            mSelectedItems.remove(which);
                                        }
                                    }
                                });
                final AlertDialog dial = builder.create();
                dial.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();
                        Marker marker;

                        if (parent.getTag() != null && parent.getTag().equals(CheckMyFacConstants.TYPE_DISTR)) {
                            marker = markerUtils.getMarkerByDescription(name); // DISTR ont comme nom la description
                        } else {
                            marker = markerUtils.getMarkerByTitle(name);
                        }
                        viewManager.moveCameraOnMarker(mMap, marker);
                        dial.dismiss();
                        return false;
                    }
                });
                dial.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();
                        Marker marker;
                        if (parent.getTag() != null && parent.getTag().equals(CheckMyFacConstants.TYPE_DISTR)) {
                            marker = markerUtils.getMarkerByDescription(name); // DISTR ont comme nom la description
                        } else {
                            marker = markerUtils.getMarkerByTitle(name);
                        }
                        if(marker!=null) {
                            //Mis à jour de la visibilité en base de donnéee
                            ((PointInteretDAO) pointInteretDAO).updateVisibilityById(getId(marker), !marker.isVisible());
                            //Mis à jour de la visibilité du marker sur la map
                            marker.setVisible(!marker.isVisible());
                        }
                    }
                });
                dial.show();
            }
        }
        return true;
    }

    /** Call when the AsyncAddMarker has finished to get and put all the PI on the map
     * @param idWithMarkers the markers list */
    @Override
    public void onTaskCompleted(Map<String, Marker> idWithMarkers) {
        this.idWithMarkers = idWithMarkers;
        markerUtils.setMapMarkers(idWithMarkers);
    }

    // call by the layout
    public void onHamburgerClick(View v) {
        if(drawer!=null) drawer.openDrawer(GravityCompat.START);
        animateFab(false);
    }

    /**
     * Changement de l'état de la map (NONE, NEW_MARKER, UPDATE_OR_DELETE_MARKER)
     * et mis-à-jour des SubActionButton
     * @param state nouvel état de la map
     */
    public void changeMapState(MapState state){
        if(!this.state.equals(state)){
            viewManager.setImageDependingState(state);
            this.state = state;
        }
    }

    /**
     * Fermeture du FloatingActionButton lors de l'ouverture de la NavigationView
     */
    @Override
    public void onDrawerStateChanged(int newState) { if(nav.isShown()) animateFab(false);   }

    /**
     * Listener permettant de créer un nouveau point d'intérêt si l'état de la map est NEW_MARKER
     * plus mis-à-jour de l'état à NONE
     * @param point position du point d'intérêt
     */
    @Override
    public void onMapClick(final LatLng point) {     ///// Choisi où placer le point
        if(isState(MapState.NEW_MARKER)){
            creationUserPoint(point, this);
        }
        changeMapState(MapState.NONE);
    }

    /**
     * Listener permettant de créer un nouveau point d'intérêt
     * plus mis-à-jour de l'état à NONE
     * @param point position du point d'intérêt
     */
    @Override
    public void onMapLongClick(final LatLng point) {     ///// Choisi où placer le point
        creationUserPoint(point, this);
        changeMapState(MapState.NONE);
    }

    /**
     * Listener permettant de modifier ou supprimer un point d'intérêt si l'état de la map est UPDATE_OR_DELETE_MARKER
     * et si le marker est déplçable (marker de l'utilisateur)
     * plus mis-à-jour de l'état à NONE
     * @param marker cliqué
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.isDraggable() && isState(MapState.UPDATE_OR_DELETE_MARKER)) { // draggable signifie User Marker
            deleteOrModifyUserPoint(this, marker);
        }
        changeMapState(MapState.NONE);
        return false;
    }

    /**
     * Listener permettant de modifier ou supprimer un point d'intérêt si le marker est déplçable (marker de l'utilisateur)
     * ou affichage  de la window correspondant aux transports
     * plus mis-à-jour de l'état à NONE
     * @param marker marker correspondant window cliqué
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.isDraggable()) { // draggable signifie User Marker
            deleteOrModifyUserPoint(this, marker);
        } else if (marker.getAlpha()==CheckMyFacConstants.ALPHA_BUS
                || marker.getAlpha()==CheckMyFacConstants.ALPHA_METRO){

            String arretName = ((TransportDAO)transportDAO).getArret(getId(marker));
            String titleDialog = marker.getTitle();
            String arrets = marker.getSnippet();
            List<String> headers = CheckMyFacUtils.splitList(arrets);
            if(headers!=null) {
                TransportKeys key = null;
                if(marker.getAlpha()==CheckMyFacConstants.ALPHA_BUS) key = TransportKeys.bus;
                else if(marker.getAlpha()==CheckMyFacConstants.ALPHA_METRO) key = TransportKeys.metros;
                horairesAdapter = viewManager.displayTransportDialog(this, key, titleDialog, arretName, headers);
            }
        }
        changeMapState(MapState.NONE);
    }

    /**
     * Listener permettant de détecter la fin du déplacement du marker par l'utilisateur et
     * lui demandant si il veux mettre à jour la position du point d'intérêt
     * @param marker déplacé
     */
    @Override
    public void onMarkerDragEnd(final Marker marker) {
        final PointInteret pi = ((PointInteretDAO) pointInteretDAO).getPI(getId(marker));
        AlertDialog.Builder areYouSure = new AlertDialog.Builder(this);
        areYouSure.setTitle(getString(R.string.EtesVousSur))
                .setCancelable(true)
                //Repositionnement du marker sur la map à son ancienne position
                .setNegativeButton(getString(R.string.Annul), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        marker.setPosition(new LatLng(pi.getLat(), pi.getLon()));
                        dialog.cancel();
                    }
                })
                //Mise à jour la position du point d'intérêt en base
                .setPositiveButton(getString(R.string.Val), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        pi.setLat((float)marker.getPosition().latitude);
                        pi.setLon(((float)marker.getPosition().longitude));
                        ((PointInteretDAO) pointInteretDAO).updatePIPosition(pi);
                    }
                });
        areYouSure.create().show();
    }

    /** Listener d'écoute des switch du drawer layout */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String type = CheckMyFacUtils.getTypeByBySwitchId(buttonView.getId());
        CheckMyFacUtils.setVisibilityByType(type, isChecked, ((PointInteretDAO) pointInteretDAO), prefs);
        List<String> listePiIds=((PointInteretDAO) pointInteretDAO).getAllPiIdsByType(type);
        markerUtils.setMarkersVisibility(listePiIds, isChecked);
    }

    /**
     * Listener d'écoute du changement de la date dans le calendrier
     */
    @Override
    public void onSelectedDayChange(@NonNull CalendarView arg0, int year, int month, int date) {
        //Janvier correspond au mois 0, février au mois 1, ..., décembre au mois 11
        String mois = String.format(Locale.getDefault(), "%02d", month+1);
        String jour = String.format(Locale.getDefault(), "%02d", date);
        dateExpiration=year+mois+jour;
    }

    /**
     * Mise à jour de la date d'expiration à la fermeture des alertDialog
     */
    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        dateExpiration = CheckMyFacConstants.sdf.format(new Date());
    }

    /**
     * Mise à jour de transportWithHoraires si la requête des horaires est réussi
     */
    @Override
    public void onSuccess(String header, List<HoraireTransport> horaires) {
        if(transportWithHoraires==null) transportWithHoraires = new LinkedHashMap<>();
        if(header != null && horaires != null){
            transportWithHoraires.put(header, horaires);
            horairesAdapter.setData(transportWithHoraires);
        }
    }

    /**
     * Affichage d'un message toast si la requête des horaires a échoué
     */
    @Override
    public void onFailure(String header) {
        viewManager.toastMessage(getString(R.string.errorHoraires)+" \""+header+'\"');
    }

    /**
     * Méthode permettant de centrer la map si l'utilisateur est dans le périmétre de l'université
     */
    @SuppressWarnings("deprecation")
    public void centerMapIfInPerimeter(){
        Location location = mMap.getMyLocation();
        viewManager.moveMapOnPositionIfInPerimeter(mMap,location);
    }

    public SharedPreferences getPrefs(){ return prefs; }
    ////////////////////// METHODES PRIVEES ///////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Ajout d'un listener à chaque switch du menu
     */
    private void setOnCheckedChangeListener(Menu menu) {
        Switch sw = (Switch)menu.findItem(R.id.nav_distributeur).getActionView();
        sw.setChecked(prefs.getBoolean(CheckMyFacUtils.getPrefConstantBySwitchId(R.id.nav_distributeur),true));
        sw.setOnCheckedChangeListener(this);
        sw = (Switch)menu.findItem(R.id.nav_restauration).getActionView();
        sw.setChecked(prefs.getBoolean(CheckMyFacUtils.getPrefConstantBySwitchId(R.id.nav_restauration),true));
        sw.setOnCheckedChangeListener(this);
        sw = (Switch)menu.findItem(R.id.nav_bibliotheque).getActionView();
        sw.setChecked(prefs.getBoolean(CheckMyFacUtils.getPrefConstantBySwitchId(R.id.nav_bibliotheque),true));
        sw.setOnCheckedChangeListener(this);
        sw = (Switch)menu.findItem(R.id.nav_user).getActionView();
        sw.setChecked(prefs.getBoolean(CheckMyFacUtils.getPrefConstantBySwitchId(R.id.nav_user),true));
        sw.setOnCheckedChangeListener(this);
    }

    /**
     * Méthode permettant d'afficher et définir le comportement de l'alertDialog pour la création d'un
     * point d'intérêt
     * @param point coordonné du point d'intérêt à créer
     */
    private void creationUserPoint(final LatLng point, final Context context){
        AlertDialog.Builder confirmation = new AlertDialog.Builder(context);
        confirmation.setTitle(context.getString(R.string.AjoutPI))
                .setCancelable(true)
                .setView(R.layout.create_marker_dialog)
                .setNegativeButton(context.getString(R.string.Annul), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(context.getString(R.string.Val), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        EditText titre = (EditText) ((Dialog) dialog).findViewById(R.id.titre_create_marker_dialog);
                        EditText descr = (EditText) ((Dialog) dialog).findViewById(R.id.desc_create_marker_dialog);
                        Spinner spinner_couleur = (Spinner) ((Dialog) dialog).findViewById(R.id.spinner_create_marker_dialog);
                        CheckBox pointExpire = (CheckBox)  ((Dialog) dialog).findViewById(R.id.view_calendar_marker_dialog);
                        if(!pointExpire.isChecked()) dateExpiration = null;
                        PointInteret pi = new PointInteret(CheckMyFacConstants.TYPE_USER, titre.getText().toString(),
                                descr.getText().toString(), (float) point.latitude, (float) point.longitude,
                                spinner_couleur.getSelectedItem().toString(), dateExpiration);
                        ((PointInteretDAO) pointInteretDAO).insertPI(pi);
                        addOrUpdateUserMarker(pi, null);
                    }
                });
        final AlertDialog alertDialogCreate = confirmation.create();

        // Callback sur le titre : Deblocage ou non du bouton "Valider"
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener(){
            public void onShow(final DialogInterface dialog){
                final Button valider = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                //On récupére le titre de tous les PI (pas seulement ceux de l'utilisateur), pour qu'il supprime que les siens
                final List<String> liste_exist = ((PointInteretDAO) pointInteretDAO).getAllNamePiByType(null);
                EditText titre = (EditText) ((AlertDialog)dialog).findViewById(R.id.titre_create_marker_dialog);
                titre.addTextChangedListener(new TextWatcher() {

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void afterTextChanged(Editable s) {}

                    public void onTextChanged(CharSequence texte, int start, int before, int count) { // Verif pendant que le texte change
                        if (count==0 || liste_exist.contains(texte.toString())){
                            valider.setEnabled(false);
                            return;
                        }
                        valider.setEnabled(true);
                    }
                });
                if(titre.length()==0) valider.setEnabled(false);
            }
        });
        alertDialogCreate.show();

        CheckBox pointExpire = (CheckBox)  alertDialogCreate.findViewById(R.id.view_calendar_marker_dialog);
        pointExpire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            CalendarView view = (CalendarView) alertDialogCreate.findViewById(R.id.calendar_date_marker_dialog);
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) view.setVisibility(View.VISIBLE);
                else view.setVisibility(View.GONE);
            }
        });

        CalendarView view = (CalendarView) alertDialogCreate.findViewById(R.id.calendar_date_marker_dialog);
        view.setVisibility(View.GONE);
        view.setOnDateChangeListener(this);
        alertDialogCreate.setOnDismissListener(this);

        //init spinner with proprosed color (AFTER alert.show !... else following findViewById return null)
        viewManager.initSpinnerCouleur(alertDialogCreate);
    }

    /**
     * Méthode permettant d'afficher et définir le comportement de l'alertDialog pour la modification
     * ou la suppression d'un point d'intérêt
     * @param marker correspondant au point d'intérêt à modifier
     */
    private void deleteOrModifyUserPoint(final Context context, final Marker marker){
        final PointInteret oldPi = ((PointInteretDAO) pointInteretDAO).getPI(getId(marker));

        AlertDialog.Builder confirmation = new AlertDialog.Builder(context);
        confirmation.setTitle(context.getString(R.string.ModifPI))
                .setCancelable(true)
                .setView(R.layout.create_marker_dialog)
                .setNegativeButton(context.getString(R.string.Supp), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        ((PointInteretDAO) pointInteretDAO).removePIWithNomAndDescr(marker.getTitle(), marker.getSnippet());
                        marker.setVisible(false);
                    }
                })
                .setPositiveButton(context.getString(R.string.Modif), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        EditText titre = (EditText) ((Dialog) dialog).findViewById(R.id.titre_create_marker_dialog);
                        EditText descr = (EditText) ((Dialog) dialog).findViewById(R.id.desc_create_marker_dialog);
                        Spinner spinner_couleur = (Spinner) ((Dialog) dialog).findViewById(R.id.spinner_create_marker_dialog);
                        CheckBox pointExpire = (CheckBox)  ((Dialog) dialog).findViewById(R.id.view_calendar_marker_dialog);

                        //Permet d'éviter la création d'un objet PointInteret inutile et de faire un appel en base de donnée
                        //si il n'y a eu aucune modification effectué
                        if( !oldPi.getName().equals(titre.toString()) || !oldPi.getDesc().equals(descr.toString())
                                || !spinner_couleur.getSelectedItem().toString().equals(oldPi.getCoul())
                                || (pointExpire.isChecked() && !oldPi.getDate().equals(dateExpiration)) ){
                            if(!pointExpire.isChecked()) dateExpiration = null;
                            PointInteret piNew = new PointInteret(CheckMyFacConstants.TYPE_USER, titre.getText().toString(),
                                    descr.getText().toString(), (float) marker.getPosition().latitude,
                                    (float) marker.getPosition().longitude, spinner_couleur.getSelectedItem().toString()
                                    ,dateExpiration);
                            ((PointInteretDAO) pointInteretDAO).updatePI(oldPi, piNew);
                            addOrUpdateUserMarker(piNew, marker);
                        }
                    }
                })
                .setNeutralButton(context.getString(R.string.Annul), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialogUpdateOrDelete = confirmation.create();
        alertDialogUpdateOrDelete.setOnShowListener(new DialogInterface.OnShowListener() // Ecoute,lors de l'affichage du dialog, si l'EditText se modifie
        {																  // pour activer ou non le bouton "Valider"
            public void onShow(final DialogInterface dialog){

                final Button valider = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                //On récupére le titre de tous les PI (pas seulement ceux de l'utilisateur), pour qu'il supprime que les siens
                final List<String> liste_exist = ((PointInteretDAO) pointInteretDAO).getAllNamePiByType(null);
                EditText titre = (EditText) ((AlertDialog)dialog).findViewById(R.id.titre_create_marker_dialog);
                titre.addTextChangedListener(new TextWatcher() {

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void afterTextChanged(Editable s) {}

                    public void onTextChanged(CharSequence texte, int start, int before, int count) { // Verif pendant que le texte change
                        if (count == 0 || (liste_exist.contains(texte.toString()) && !texte.toString().equals(oldPi.getName()))){
                            valider.setEnabled(false);
                            return;
                        }
                        valider.setEnabled(true);
                    }
                });
                if(titre.length()==0 && !titre.toString().equals(oldPi.getName())) valider.setEnabled(false);
            }
        });
        alertDialogUpdateOrDelete.show();

        // Init existing values
        ((EditText)alertDialogUpdateOrDelete.findViewById(R.id.titre_create_marker_dialog)).setText(marker.getTitle());
        ((EditText)alertDialogUpdateOrDelete.findViewById(R.id.desc_create_marker_dialog)).setText(marker.getSnippet());

        CheckBox pointExpire = (CheckBox) alertDialogUpdateOrDelete.findViewById(R.id.view_calendar_marker_dialog);
        pointExpire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            CalendarView view = (CalendarView) alertDialogUpdateOrDelete.findViewById(R.id.calendar_date_marker_dialog);
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    view.setVisibility(View.VISIBLE);
                    if(oldPi.getDate() == null || oldPi.getDate().isEmpty()) dateExpiration = CheckMyFacConstants.sdf.format(new Date());
                    else dateExpiration = oldPi.getDate();
                }
                else view.setVisibility(View.GONE);
            }
        });

        CalendarView view = (CalendarView) alertDialogUpdateOrDelete.findViewById(R.id.calendar_date_marker_dialog);
        Date date = CheckMyFacUtils.getDatePi(oldPi);
        if(date != null){
            view.setDate(date.getTime());
            pointExpire.setChecked(true);
        }else view.setVisibility(View.GONE);

        Spinner spinner_couleur = viewManager.initSpinnerCouleur(alertDialogUpdateOrDelete);
        if (spinner_couleur != null && spinner_couleur.getAdapter()!=null && oldPi.getCoul()!=null) {
            @SuppressWarnings("unchecked")
            int spinnerPosition = ((ArrayAdapter<String>)spinner_couleur.getAdapter()).getPosition(oldPi.getCoul());
            spinner_couleur.setSelection(spinnerPosition);
        }

        view.setOnDateChangeListener(this);
        alertDialogUpdateOrDelete.setOnDismissListener(this);
    }

    /**
     * find the id of {@link PointInteret} or {@link com.checkmyfac.objet.Transport} by the Marker
     * @param marker the marker
     * @return the id, or null if not known
     */
    private String getId(Marker marker) {
        String res = null;
        if(marker != null && idWithMarkers != null){
            for(Map.Entry<String, Marker> entry : idWithMarkers.entrySet()){
                if(entry.getValue().getId().equals(marker.getId())){
                    res = entry.getKey();
                    break;
                }
            }
        }
        return res;
    }

    private boolean isState(MapState state) {
        return this.state==state;
    }

    private void addOrUpdateUserMarker(PointInteret pi, Marker marker){
        Marker newMarker = viewManager.addOrUpdateMarkerUserOnMap(mMap, pi, marker);
        if(newMarker!=null){
            idWithMarkers.put(pi.getId(), newMarker);
            newMarker.showInfoWindow();
        }
    }

    private void animateFab(boolean toOpen){
        viewManager.animateFAB(toOpen, fab, menu);
    }


    ////////////////////// CALLBACKS NON UTILISES ///////////////////////
    /////////////////////////////////////////////////////////////////////
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) { }

    @Override
    public void onDrawerOpened(View drawerView) {  }

    @Override
    public void onDrawerClosed(View drawerView) { }

    @Override
    public void onMarkerDragStart(Marker marker) {  }

    @Override
    public void onMarkerDrag(Marker marker) {   }

}
