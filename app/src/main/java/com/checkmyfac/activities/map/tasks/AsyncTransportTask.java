package com.checkmyfac.activities.map.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.checkmyfac.R;
import com.checkmyfac.activities.map.transport.TransportJSONParser;
import com.checkmyfac.activities.map.transport.TransportKeys;
import com.checkmyfac.activities.map.transport.TransportURLFormater;
import com.checkmyfac.interfaces.OnTransportsCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AsyncTransportTask extends AsyncTask<Void, Void, List<HoraireTransport>> {

    private static final String TAG = "AsyncTransportTask";

    private TransportKeys type;
    private final String titleProgress, nomArret, ligne;
    private Context context;
    private OnTransportsCallback httpListener;
    private ProgressDialog progress;

    public AsyncTransportTask(Context context, TransportKeys type, String titleProgress,
                              String ligne, String nomArret) {
        this.context = context;
        this.titleProgress = titleProgress;
        this.type = type;
        this.nomArret = nomArret;
        this.ligne = ligne;
    }

    public void setHttpListener(OnTransportsCallback callback) {
        this.httpListener = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress = new ProgressDialog(context);
        progress.setTitle(context.getString(R.string.horairesProgress)+" \""+
                titleProgress+" - "+type.display(context)+' '+ligne+'\"');
        progress.setIndeterminate(false);
        progress.show();
    }

    @Override
    protected List<HoraireTransport> doInBackground(Void... params) {
        List<HoraireTransport> horaires;
        try {
            String urlDest = TransportURLFormater.getLigne(type, ligne);
            JSONObject res = sendGet(urlDest);
            Map<String, String> destinationsSlugsAndNames = TransportJSONParser.getDestinations(res);

            horaires = new ArrayList<>(destinationsSlugsAndNames.size());

            for(Map.Entry entry : destinationsSlugsAndNames.entrySet()){
                String urlHoraires = TransportURLFormater.getHoraires(type, ligne, nomArret, (String)entry.getKey());
                System.out.println(urlHoraires);
                JSONObject resHoraires = sendGet(urlHoraires);
                List<String> times = TransportJSONParser.getHoraires(resHoraires);
                for(String time : times){
                    horaires.add(new HoraireTransport((String)entry.getValue(), time));
                }
            }
        } catch (Exception e){
            Log.w(TAG, e.toString());
            return null;
        }
        return horaires;
    }

    @Override
    protected void onPostExecute(List<HoraireTransport> horaires) {
        progress.dismiss();
        if(httpListener==null) return;
        if(horaires==null) this.httpListener.onFailure(nomArret);
        this.httpListener.onSuccess(ligne, horaires);
    }

    private static JSONObject sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setReadTimeout(15000);
        con.setConnectTimeout(15000); // 15 secondes

        StringBuilder response = new StringBuilder();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return new JSONObject(response.toString());
    }

}