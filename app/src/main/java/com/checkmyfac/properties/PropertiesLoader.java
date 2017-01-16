package com.checkmyfac.properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.checkmyfac.R;
import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.checkmyfac.dao.transport.TransportDAO;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.objet.Transport;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesLoader implements PropertiesLoaderInterface {

    private static final String TAG = "PropertiesLoader";

    private static final String DIR_FORMS = "Formulaires";
    private static final String DIR_SEPARATOR = "/";
    private static final String PROPERTIES_SEPARATOR = "##";
    private static final String PROPERTIES_EXTENSION = ".properties";

    private Properties properties;
    private Context context;

    public PropertiesLoader(Context context) {
        this.context = context;
        properties = new Properties();
        try{
            AssetManager asset = context.getAssets(); // do not close !
            InputStream in;
            Reader reader;

            String filename = getFichierParChoix();
            try {
                in = asset.open(DIR_FORMS + DIR_SEPARATOR + filename);     // On cherche dans le dossier assets/Formulaires
                reader = new InputStreamReader(in, "UTF-8");
                properties.load(reader);
            } catch (IOException io) {
                // Ya pas !
            } catch (Resources.NotFoundException e) {
                Log.wtf(TAG, "Did not find raw resource: " + e);
            }
        } catch (Exception e){
            Log.w(TAG, e);
        }
    }

    /** Méthode récupérant l'adresse officiel de la fac dans le fichier properties */
    @Override
    public String getOfficialAddress() {
        if (properties != null) {
            return properties.getProperty("Adresse", null);
        }
        return null;
    }

    /** Méthode permettant de récupérer la latitude de l'université */
    @Override
    public Double getLatFac() {
        if (properties != null) {
            String str = properties.getProperty("Latitude");
            try {
                return Double.parseDouble(str);
            } catch (NullPointerException ignored) {}
        }
        return null;
    }

    /** Méthode permettant de récupérer la longitude de l'université */
    @Override
    public Double getLonFac() {
        if (properties != null) {
            String str = properties.getProperty("Longitude");
            try {
                return Double.parseDouble(str);
            } catch (NullPointerException ignored) {}
        }
        return null;
    }

    /** Méthode permettant de récupérer le zoom à appliquer à la GoogleMap */
    @Override
    public Float getZoom() {
        if (properties != null) {
            String str = properties.getProperty("Zoom");
            try {
                return Float.parseFloat(str);
            } catch (NullPointerException ignored) {}
        }
        return null;
    }

    /** Méthode permettant de récupérer la rotation à appliquer à la GoogleMap */
    @Override
    public Float getRotation() {
        if (properties != null) {
            String str = properties.getProperty("Rotation");
            try {
                return Float.parseFloat(str);
            } catch (NullPointerException ignored) { }
        }
        return null;
    }

    /** Méthode permettant de récupérer la couleur des distibuteurs définit dans le fichier properties */
    @Override
    public String getCouleurDistr() {
        if (properties != null) {
            return properties.getProperty("COULEUR_DEFAULT.DISTRIBUTEUR", null);
        }
        return null;
    }

    /** Méthode permettant de récupérer la couleur des points de restauration définit dans le fichier properties */
    @Override
    public String getCouleurRest() {
        if (properties != null) {
            return properties.getProperty("COULEUR_DEFAULT.RESTAURANT", null);
        }
        return null;
    }

    /** Méthode permettant de récupérer la couleur des bibiliotheques définit dans le fichier properties */
    @Override
    public String getCouleurBu() {
        if (properties != null) {
            return properties.getProperty("COULEUR_DEFAULT.BIBLIOTHEQUE", null);
        }
        return null;
    }

    /** Méthode permettant de récupérer la couleur des bus définit dans le fichier properties */
    @Override
    public String getCouleurBus(){
        if (properties != null) {
            return properties.getProperty("COULEUR_DEFAULT.BUS", null);
        }
        return null;
    }

    /** Méthode permettant de récupérer la couleur des métro définit dans le fichier properties */
    @Override
    public String getCouleurMetro() {
        if (properties != null) {
            return properties.getProperty("COULEUR_DEFAULT.METRO", null);
        }
        return null;
    }

    /** Méthode récupérant l'ensemble des bibliotheques */
    @Override
    public List<PointInteret> getBibliotheques() {
        return getListPIbyTypeFromProperties(CheckMyFacConstants.BIBLIOTHEQUE);
    }

    /** Méthode récupérant l'ensemble des distributeurs */
    @Override
    public List<PointInteret> getDistributeurs() {
        return getListPIbyTypeFromProperties(CheckMyFacConstants.DISTRIBUTEUR);
    }

    /** Méthode récupérant l'ensemble des points de restauration */
    @Override
    public List<PointInteret> getRestauration() {
        return getListPIbyTypeFromProperties(CheckMyFacConstants.RESTAURANT);
    }

    /** Méthode récupérant l'ensemble des points de bus */
    @Override
    public List<Transport> getBus() {
        return getListTransportsByTypeFromProperties(CheckMyFacConstants.TYPE_BUS);
    }

    /** Méthode récupérant l'ensemble des points de métro */
    @Override
    public List<Transport> getMetro() {
        return getListTransportsByTypeFromProperties(CheckMyFacConstants.TYPE_METRO);
    }


    /**
     * Cette fonction charge les données (points, couleurs, etc...) du fichier properties (la fac
     * choisie) et insère en base les points par défaut, et les couleurs en preference (
     * @param prefs les shared preferences
     */
    @Override
    public void insertionInDBandPrefsFromProperties(SharedPreferences prefs) {
        Log.d(TAG, "insertionFromProperties");

        PointInteretDAO pointInteretDAO = new PointInteretDAO(context);
        pointInteretDAO.insertListPI(getBibliotheques());
        pointInteretDAO.insertListPI(getRestauration());
        pointInteretDAO.insertListPI(getDistributeurs());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CheckMyFacConstants.PREF_COLOR_BU, getCouleurBu());
        editor.putString(CheckMyFacConstants.PREF_COLOR_REST, getCouleurRest());
        editor.putString(CheckMyFacConstants.PREF_COLOR_DISTR, getCouleurDistr());
        editor.putString(CheckMyFacConstants.PREF_COLOR_BUS, getCouleurBus());
        editor.putString(CheckMyFacConstants.PREF_COLOR_METRO, getCouleurMetro());
        editor.apply();

        TransportDAO transportDAO = new TransportDAO(context);
        transportDAO.insertListTransport(getMetro());
        transportDAO.insertListTransport(getBus());
    }

    public String[] getAllFacNames(){
        try{
            AssetManager asset = context.getAssets(); // do not close !
            String[] res = asset.list(DIR_FORMS);
            if(res.length == 0) return new String[0];
            else{
                for(int i=0; i<res.length; i++) res[i] = res[i].replace(PROPERTIES_EXTENSION, "");
                return res;
            }
        } catch (IOException e){
            return null;
        }
    }


    //////////// METHODES PRIVEES //////////////

    /**
     * Retourne le nom du fichier correspondant au choix de fac de l'utilisateur
     * @return le nom du fichier properties
     */
    private String getFichierParChoix() {
        SharedPreferences pref = context.getSharedPreferences(CheckMyFacConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getString(CheckMyFacConstants.THE_FAC, null)+PROPERTIES_EXTENSION;
    }

    /**
     * Retourne tous les points d'interets, ayant comme clef la chaine passée en paramètre, contenu dans les properties.
     * @param type la clef
     * @return la liste des points d'interets valides
     */
    private List<PointInteret> getListPIbyTypeFromProperties(String type) {

        List<String> values_Prop = getListValuesByTypeFromProperties(type);

        List<PointInteret> listPI = new ArrayList<>();
        String[] tab_tmp;
        PointInteret pi_tmp;

        String type_pi;
        switch (type) {
            case CheckMyFacConstants.BIBLIOTHEQUE:
                type_pi = CheckMyFacConstants.TYPE_BU;
                break;
            case CheckMyFacConstants.RESTAURANT:
                type_pi = CheckMyFacConstants.TYPE_REST;
                break;
            case CheckMyFacConstants.DISTRIBUTEUR:
                type_pi = CheckMyFacConstants.TYPE_DISTR;
                break;
            default:
                type_pi = null;
                break;
        }

        for (String str : values_Prop) {

            tab_tmp = str.split(PROPERTIES_SEPARATOR);
            pi_tmp = new PointInteret();
            try {
                pi_tmp.setType(type_pi);
                pi_tmp.setName(tab_tmp[0].trim());
                pi_tmp.setDesc(tab_tmp[1].trim());
                pi_tmp.setLat(Float.parseFloat(tab_tmp[2].trim()));
                pi_tmp.setLon(Float.parseFloat(tab_tmp[3].trim()));
            } catch (IndexOutOfBoundsException | NumberFormatException | NullPointerException ignored) {
                // Log pas assez de paramètres
            }
            String couleur = properties.getProperty(CheckMyFacConstants.COULEUR_DEFAULT + '.' + type);
            if ( !couleur.isEmpty() ) {
                pi_tmp.setCoul(couleur);
            }

            listPI.add(pi_tmp);
        }
        return listPI;
    }

    /**
     * Retourne tous les transports, ayant comme clef la chaine passée en paramètre, contenu dans les properties.
     * @param type la clef
     * @return la liste des transports valides
     */
    private List<Transport> getListTransportsByTypeFromProperties(String type) {

        List<String> values_Prop = getListValuesByTypeFromProperties(type);

        List<Transport> listTransport = new ArrayList<>();
        String[] tab_tmp;
        Transport transport_tmp;
        String prefixe_title = "";

        for (String str : values_Prop) {

            tab_tmp = str.split(PROPERTIES_SEPARATOR);
            transport_tmp = new Transport();
            try {
                transport_tmp.setType(type);

                if(type.equals(CheckMyFacConstants.TYPE_BUS))
                    prefixe_title = context.getString(R.string.Arret);
                else if(type.equals(CheckMyFacConstants.TYPE_METRO))
                    prefixe_title = context.getString(R.string.Station);

                transport_tmp.setDescription(prefixe_title+' '+tab_tmp[0].trim());
                transport_tmp.setNumero(CheckMyFacUtils.splitInteger(tab_tmp[1].trim()));
                transport_tmp.setArret(tab_tmp[2].trim().replaceAll(" ", "+"));
                transport_tmp.setLat(Float.parseFloat(tab_tmp[3].trim()));
                transport_tmp.setLon(Float.parseFloat(tab_tmp[4].trim()));
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                e.printStackTrace();
                // Log pas assez de paramètres
            }

            String couleur = properties.getProperty(CheckMyFacConstants.COULEUR_DEFAULT + '.' + type);
            if ( couleur!=null && !couleur.isEmpty() ) {
                transport_tmp.setCouleur(couleur);
            }

            listTransport.add(transport_tmp);
        }
        return listTransport;

    }

    /**
     * Retourne la liste des valeurs contenus dans les properties ayant comme clef la chaine passée en paramètre
     * @param type le nom
     * @return la liste des noms correspondants à la clé passée en parametre
     */
    private List<String> getListValuesByTypeFromProperties(String type) {
        List<String> values_Prop = new ArrayList<>();

        boolean finish = false;
        int i = 1;
        String string_tmp;
        while (!finish) {
            string_tmp = properties.getProperty(type + '.' + i);
            if (string_tmp == null || string_tmp.isEmpty()) {
                finish = true;
            } else {
                values_Prop.add(string_tmp);
                i++;
            }
        }

        return values_Prop;
    }

}