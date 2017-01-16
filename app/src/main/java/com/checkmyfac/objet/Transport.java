package com.checkmyfac.objet;

public class Transport {

    private String id;
    private String type;
    private String description;
    private Integer[] numeros;
    private String arret;
    private Float latitude;
    private Float longitude;
    private String couleur;

    public Transport(){}

    public Transport(String description, String type, Integer[] numeros, String arret,
                     Float latitude, Float longitude, String couleur) {
        this.description = description;
        this.type = type;
        this.numeros = numeros;
        this.arret = arret;
        this.latitude = latitude;
        this.longitude = longitude;
        this.couleur = couleur;
    }

    public Transport(String id, String description, String type, Integer[] numeros, String arret,
                     Float latitude, Float longitude, String couleur) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.numeros = numeros;
        this.arret = arret;
        this.latitude = latitude;
        this.longitude = longitude;
        this.couleur = couleur;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer[] getNumero() {
        return numeros;
    }

    public void setNumero(Integer[] numeros) {
        this.numeros = numeros;
    }

    public String getArret(){
        return arret;
    }

    public void setArret(String arret){
        this.arret = arret;
    }

    public Float getLat() {
        return latitude;
    }

    public void setLat(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLon() {
        return longitude;
    }

    public void setLon(Float longitude) {
        this.longitude = longitude;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

}
