package com.checkmyfac.activities.map.tasks;

public class HoraireTransport {

    private String destination;
    private String horaire;

    HoraireTransport(String destination, String horaire){
        this.destination = destination;
        this.horaire = horaire;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getHoraire() {
        return horaire;
    }

    public void setHoraire(String horaire) {
        this.horaire = horaire;
    }
}
