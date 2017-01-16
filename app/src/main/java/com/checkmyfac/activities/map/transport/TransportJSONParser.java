package com.checkmyfac.activities.map.transport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transforme les réponses JSON de l'API
 */
public class TransportJSONParser {

    private TransportJSONParser(){}

    /**
     * Methode permettant de récuperer les lignes de transport et leurs destinations en fonction du json
     * @param jsonObject à parser pour récupérer la map
     * @return la map faisant le lien entre la ligne et sa destination
     * @throws JSONException
     */
    public static Map<String, String> getDestinations(JSONObject jsonObject) throws JSONException {
        if(jsonObject==null) return null;

        Map<String, String> destinationsSlugsAndNames = new HashMap<>();
        JSONArray array = jsonObject.getJSONObject("response").getJSONArray("destinations");
        if(array!=null){
            for(int i=0; i<array.length(); i++){
                JSONObject oneDest = array.getJSONObject(i);
                System.out.println(oneDest);
                if(oneDest!=null){
                    destinationsSlugsAndNames.put(oneDest.getString("slug"), oneDest.getString("destination"));
                }
            }
        }
        return destinationsSlugsAndNames;
    }

    /**
     * Methode retournant l'ensemble des horaires à partir du json passé en argument
     * @return liste des horaires
     * @throws JSONException
     */
    public static List<String> getHoraires(JSONObject jsonObject) throws JSONException {
        List<String> horaires = null;
        JSONArray array = jsonObject.getJSONObject("response").getJSONArray("schedules");
        if(array!=null){
            int maxSize = 2;
            if(array.length()<maxSize) maxSize=array.length();
            horaires = new ArrayList<>(maxSize);

            for(int i=0; i<maxSize; i++){
                JSONObject oneDest = array.getJSONObject(i);
                System.out.println(oneDest);
                if(oneDest!=null && !oneDest.getString("message").contains(".......")){
                    horaires.add(oneDest.getString("message"));
                }
            }
        }
        return horaires;
    }

}
