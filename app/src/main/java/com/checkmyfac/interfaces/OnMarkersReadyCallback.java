package com.checkmyfac.interfaces;

import com.google.android.gms.maps.model.Marker;

import java.util.Map;

public interface OnMarkersReadyCallback {

    void onTaskCompleted(Map<String, Marker> idWithMarkers);

}