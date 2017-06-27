package com.tmc_technology.oauth_2;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by jerem on 20/06/2017.
 */



public class GetAccessUri {
    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONArray jObjURI = null;
    static String json ;
    public GetAccessUri() { }

    DefaultHttpClient httpClientUri;
    HttpGet request;

    public JSONObject geturi(String address,String token) {


        try {
            // DefaultHttpClient
            httpClientUri = new DefaultHttpClient();

            //Request
            String message = (address + "?access_token=" + token);
            request = new HttpGet(message);

            // Sends the request
            HttpResponse httpResponse = httpClientUri.execute(request);

            // Get answer
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();

            // convert answer in String format
            json = sb.toString();
            Log.i("JSONStr", json);

        } catch (Exception e) {
            e.getMessage();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // Parse the String to a JSON Object
        try {
            jObjURI = new JSONArray(json);
            for(int i=0;i<jObjURI.length();i++){
                jObj = jObjURI.getJSONObject(i);

                // Requeste Result
                Log.d("json GetAccessUri : ", jObj.toString());
                Log.d("Uri ",jObj.getString("uri"));
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // Return JSON String of last index
        return jObj;
    }

}