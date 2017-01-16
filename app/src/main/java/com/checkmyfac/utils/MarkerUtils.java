package com.checkmyfac.utils;

import android.content.Context;

import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarkerUtils {

    private Context context;
    private Map<String, Marker> idWithMarkers;

    public MarkerUtils(Context context, Map<String, Marker> idWithMarkers) {
        this.context = context;
        this.idWithMarkers=idWithMarkers;
    }

    public Map<String, Marker> getMapMarkers(){
        return idWithMarkers;
    }

    public void setMapMarkers(Map<String, Marker> idWithMarkers){
        this.idWithMarkers = idWithMarkers;
    }

    public Marker getMarkerByTitle(String title){
        if(idWithMarkers!=null) {
            for (Map.Entry<String, Marker> entry : idWithMarkers.entrySet()) {
                if (entry.getValue().getTitle().equalsIgnoreCase(title)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Marker getMarkerByDescription(String description){
        if(idWithMarkers!=null) {
            for (Map.Entry<String, Marker> entry : idWithMarkers.entrySet()) {
                if (entry.getValue().getSnippet().equalsIgnoreCase(description)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public List<Marker> getMarkersByType(PointInteretDAO pointInteretDAO, String type) {
        if(type == null || type.isEmpty() || idWithMarkers == null) return null;
        List<Marker> markersByType = new ArrayList<>();
        Marker tmp ;
        for(String id : pointInteretDAO.getAllPiIdsByType(type)){
            tmp = idWithMarkers.get(id);
            if(tmp != null) markersByType.add(tmp);
        }
        return markersByType;
    }

    public void setMarkersIcon(List<Marker> markers, BitmapDescriptor bitmapDescriptor) {
        if(markers != null && bitmapDescriptor != null){
            for(Marker mark : markers) mark.setIcon(bitmapDescriptor);
        }
    }

    public void setMarkersVisibility(List<String> listePiIds, boolean isChecked) {
        if (listePiIds != null){
            for (String id : listePiIds) {
                if (id != null) {
                    Marker m = idWithMarkers.get(id);
                    if (m != null) m.setVisible(isChecked);
                }
            }
        }
    }
}
