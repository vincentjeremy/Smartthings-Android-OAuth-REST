package com.tmc_technology.oauth_2;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerem on 20/06/2017.
 */



public class SendParameter {
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json ;

    DefaultHttpClient httpClientUri;
    HttpPost request;

    public JSONObject sendParameter(String address,String token,String action) {


        try {
            // DefaultHttpClient
            httpClientUri = new DefaultHttpClient();

            //Request
            String message = (address + "/" + "changeheat" + "?access_token=" + token );
            Log.d("Url envoyée ", message);
            request = new HttpPost(message);

            // List of parameters sent
            params.add(new BasicNameValuePair("name"     , "kitchen"));
            params.add(new BasicNameValuePair("temp_act" , "19.1"));
            params.add(new BasicNameValuePair("temp_went", "21.5"));
            params.add(new BasicNameValuePair("timed1"   ,   "0"));
            params.add(new BasicNameValuePair("timed2"   , "240"));
            params.add(new BasicNameValuePair("timed3"   ,   "0"));
            params.add(new BasicNameValuePair("timed4"   ,   "0"));
            params.add(new BasicNameValuePair("timed5"   ,   "0"));
            params.add(new BasicNameValuePair("timed6"   ,   "0"));

            // Addition of parameters
            request.setEntity(new UrlEncodedFormEntity(params));

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

            json = sb.toString();

            // Remove the "n" useless sent by the smartapp (Changes according to the received)
            json = json.replace("n ", "");
            json = json.replace("n}n", "}");

            Log.i("JSONStr", json);
        } catch (Exception e) {
            e.getMessage();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // Parse the String to a JSON Object
        try {
            jObj = new JSONObject(json);

            // Requeste Result (Checking the correct transmission)
            Log.d("json GetDevices : ", jObj.toString());
            Log.d("Name room       ",jObj.getString("room_name"));
            Log.d("Name status     ",jObj.getString("status"));
            Log.d("Name temps act  ",jObj.getString("temp_act"));
            Log.d("Name temps went ",jObj.getString("temp_went"));
            Log.d("Name timed 1    ",jObj.getString("timed1"));
            Log.d("Name timed 2    ",jObj.getString("timed2"));
            Log.d("Name timed 3    ",jObj.getString("timed3"));
            Log.d("Name timed 4    ",jObj.getString("timed4"));
            Log.d("Name timed 5    ",jObj.getString("timed5"));
            Log.d("Name timed 6    ",jObj.getString("timed6"));

        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // Return JSON String of last index
        return jObj;
    }

}