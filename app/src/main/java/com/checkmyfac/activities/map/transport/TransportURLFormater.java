package com.checkmyfac.activities.map.transport;

/**
 * Se referer à github.com/pgrimaud/horaires-ratp-api
 */
public class TransportURLFormater {

    private TransportURLFormater(){}

    private static final String prefixe = "http://api-ratp.pierre-grimaud.fr/v2/";

    public static String getLigne(TransportKeys typeLigne){
        return prefixe+typeLigne;
    }

    public static String getLigne(TransportKeys typeLigne, String ligneName){
        return getLigne(typeLigne)+'/'+ligneName;
    }

    public static String getHoraires(TransportKeys typeLigne, String ligneName, String station,
                                     String destination){
        return getLigne(typeLigne, ligneName)+"/stations/"+station+"?destination="+destination;
    }

}
