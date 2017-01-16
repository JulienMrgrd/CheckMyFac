package com.checkmyfac.interfaces;

import com.checkmyfac.activities.map.tasks.HoraireTransport;

import java.util.List;

public interface OnTransportsCallback {

    void onSuccess(String header, List<HoraireTransport> horaires);
    void onFailure(String header);
}
