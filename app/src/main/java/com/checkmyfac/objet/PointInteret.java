package com.checkmyfac.objet;

public class PointInteret {

	private String id;
	private String type;
	private String nom;
	private String description;
	private Float lat;
	private Float lon;
	private String couleur;
	private boolean visible;
	private String date;

	public PointInteret(){
		visible=true; // visible by default
	}

	public PointInteret(String type, String nom, String description, float lat, float lon, String couleur, String date){
		this.type=type;
		this.nom=nom;
		this.description=description;
		this.lat=lat;
		this.lon=lon;
		this.couleur=couleur;
		this.visible=true;
		this.date=date;
	}

	public PointInteret(String id, String type, String nom, String description, float lat, float lon,
						String couleur, String date){
		this.id=id;
		this.type=type;
		this.nom=nom;
		this.description=description;
		this.lat=lat;
		this.lon=lon;
		this.couleur=couleur;
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type=type;
	}

	public String getName(){
		return nom;
	}

	public void setName(String nom){
		this.nom=nom;
	}

	public String getDesc(){
		return description;
	}

	public void setDesc(String description){
		this.description=description;
	}

	public Float getLon(){
		return lon;
	}

	public void setLon(Float lon){
		this.lon=lon;
	}

	public Float getLat(){
		return lat;
	}

	public void setLat(Float lat){
		this.lat=lat;
	}

	public String getCoul(){
		return couleur;
	}

	public void setCoul(String couleur){
		this.couleur=couleur;
	}

	public boolean isVisible() { return visible; }

	public void setVisible(boolean visible) { this.visible = visible; }

	public String getDate(){ return date; }

	public void setDate(String date) { this.date=date; }

	public String toString(){
		return type+' '+nom;
	}

}