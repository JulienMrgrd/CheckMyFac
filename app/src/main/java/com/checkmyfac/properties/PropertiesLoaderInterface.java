package com.checkmyfac.properties;

import android.content.SharedPreferences;

import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.objet.Transport;

import java.util.List;

public interface PropertiesLoaderInterface {

    String getOfficialAddress();
    Double getLatFac();
    Double getLonFac();
    Float getZoom();
    Float getRotation();
    String getCouleurBu();
    String getCouleurDistr();
    String getCouleurRest();
    String getCouleurBus();
    String getCouleurMetro();
    List<PointInteret> getBibliotheques();
    List<PointInteret> getDistributeurs();
    List<PointInteret> getRestauration();

    List<Transport> getBus();
    List<Transport> getMetro();

    void insertionInDBandPrefsFromProperties(SharedPreferences prefs);
}
