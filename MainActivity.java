package com.tmc_technology.oauth_2;

/**
 * Copyright (c) 2017 jemsdu31@hotmail.fr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

 /**
 * @author jemsdu31@hotmail.fr
 */

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

/****************************************************************************************************************/
/** PARAMETERS ST Account  **************************************************************************************/
/****************************************************************************************************************/


    private static String CLIENT_ID = "YOUR-CLIENT_ID";
    // Use your own client id
    private static String CLIENT_SECRET ="YOUR-CLIENT_SECRET";
    // Use your own client secret
    private static String REDIRECT_URI="http://localhost:4567/oauth/callback";
    private static String GRANT_TYPE="authorization_code";
    private static String TOKEN_URL ="https://graph.api.smartthings.com/oauth/token";
    private static String OAUTH_URL ="https://graph.api.smartthings.com/oauth/authorize";
    private static String ENDPOINT_URL ="https://graph.api.smartthings.com/api/smartapps/endpoints";
    private static String OAUTH_SCOPE="app";
    private static String SERVEUR_URI="https://www.googleapis.com/auth/urlshortener";
    // Change if you would

    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;
    String authCodeToken;
    String token = "null";
    String uriRequest = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Access =(TextView)findViewById(R.id.Access);
        auth = (Button)findViewById(R.id.auth);

/****************************************************************************************************************/
/** CREATION of the ST connection  ******************************************************************************/
/****************************************************************************************************************/
        /**Get Authorization Code
         **
         ** GET https://graph.api.smartthings.com/oauth/authorize?
         **       response_type=code&
         **       client_id=YOUR-SMARTAPP-CLIENT-ID&
         **       scope=app&
         **       redirect_uri=YOUR-SERVER-URI
         **/

        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                auth_dialog = new Dialog(MainActivity.this);
                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView)auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);

                // check request
                String info =(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE+"&redirect_uri="+SERVEUR_URI);
                Log.d("URL d'envois = ",info);

                // Loading of the Smartthing Webside : For authorization
                web.loadUrl(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE+"&redirect_uri="+SERVEUR_URI);
                web.setWebViewClient(new WebViewClient() {

                    boolean authComplete = false;
                    Intent resultIntent = new Intent();

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon){
                        super.onPageStarted(view, url, favicon);

                    }
                    String authCode;
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        // Check if the answer contains a code
                        if (url.contains("?code=") && authComplete != true) {
                            // check answer
                            Log.d("Fermeture URL = ",url);

                            Uri uri = Uri.parse(url);

                            // Code recovery
                            authCodeToken = uri.getQueryParameter("code");

                            // check Code
                            Log.d("Fermeture URI = ",authCodeToken);

                            authComplete = true;
                            resultIntent.putExtra("code", authCodeToken);

                            MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);

                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("Code", authCodeToken);
                            edit.commit();
                            auth_dialog.dismiss();

                            // Application by Token
                            Log.e("avancement : ", "Get Token ");
                            new TokenGet().execute();

                            //Toast.makeText(getApplicationContext(),"Authorization Code is: " +authCodeToken, Toast.LENGTH_SHORT).show();

                        }else if(url.contains("error=access_denied")){
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                            auth_dialog.dismiss();
                        }
                    }
                });

                auth_dialog.show();
                auth_dialog.setCancelable(true);
            }
        });
    }

/****************************************************************************************************************/
/** TOKEN Recovery  *********************************************************************************************/
/****************************************************************************************************************/

    /** Get Access Token
     **
     ** (Use the authorization code, along with the client ID and secret, to get the access token.)
     **
     ** POST https://graph.api.smartthings.com/oauth/token
     **
     **   The following parameters should be sent on the request:
     **
     **     grant_type         : use "code" for this flow.
     **     authorization_code : this is the authorization code obtained from the previous step.
     **     client_id          : this is the client id of the SmartApp. It is the identifier for the SmartApp.
     **     client_secret      : the OAuth client secret of the SmartApp.
     **     redirect_uri       : the URI of the server that will receive the token. This must match the URI you used to obtain the authorization code.
     **/

    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting SmartThings ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            // Recovery of the ST answer in Json format
            JSONObject json = jParser.gettoken(TOKEN_URL, GRANT_TYPE, authCodeToken, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

            Log.i("json : ", json.toString());

            /** GET requests to https://graph.api.smartthings.com/oauth/token will also work, but POST is preferred.
             ** That will return a response like:
             **  {
             **      "access_token": "XXXXXXXXXXX",
             **      "expires_in"  : 1576799999,
             **      "token_type"  : "bearer"
             **  }
             **/

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {

                try {
                    // Recovery date of Json
                    String tok = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String type = json.getString("token_type");

                    Log.d("Token Access", tok);
                    Log.d("Expire", expire);
                    Log.d("token_type", type);

                    auth.setText("Authenticated");
                    Access.setText("Access Token:" + tok + "\nExpires:" + expire + "\nToken type:" + type);

                    // Passage as a general parameter
                    token = tok;

                    // recovery EndPoint
                    Log.e("avancement : ", "Get EndPoint ");
                    new EndPointGet().execute();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Token Get Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }


/****************************************************************************************************************/
/** ENDPOINT Recovery  ******************************************************************************************/
/****************************************************************************************************************/

        /** Get SmartApp Endpoints
         **
         ** Using the access token, get the endpoint for the SmartApp:
         **
         ** GET -H "Authorize: Bearer ACCESS-TOKEN" "https://graph.api.smartthings.com/api/smartapps/endpoints"
         **/

    private class EndPointGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting SmartThings ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
            }

        @Override
        protected JSONObject doInBackground(String... args) {                       //UriGet
            GetAccessUri jParser = new GetAccessUri();
            JSONObject jsonUri = jParser.geturi(ENDPOINT_URL,token);

            Log.i("json : ", jsonUri.toString());

            /** That will return a response like:
             **  {
             **    "oauthClient": {
             **        "clientSecret": "CLIENT-SECRET",
             **        "clientId": "CLIENT-ID"
             **    },
             **    "uri": "BASE-URL/api/smartapps/installations/INSTALLATION-ID",
             **    "base_url": "BASE-URL",
             **    "url": "/api/smartapps/installations/INSTALLATION-ID"
             **  }
             **/

            return jsonUri;
        }

        @Override
        protected void onPostExecute(JSONObject jsonUri) {                          //UriGet
            pDialog.dismiss();
            if (jsonUri != null){

                try {
                    uriRequest = jsonUri.getString("uri");

                    Log.d("Token Access", uriRequest);

                    auth.setText("Authenticated");

                    Access.setText("uri :"+uriRequest);

                    Log.i("json Uri: ", jsonUri.toString());

                    // Communication request
                    Log.e("avancement : ", "DevicesGet ");
                    new DevicesGet().execute();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Uri Get Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

/****************************************************************************************************************/
/** REQUEST  ****************************************************************************************************/
/****************************************************************************************************************/

    /** Get Smartapp Request
     **
     ** Using the access token, get the endpoint for the SmartApp:
     **
     ** GET -H "Authorize: Bearer ACCESS-TOKEN" "BASE-URL/api/smartapps/installations/INSTALLATION-ID/Path(request)"
     **/


    private class DevicesGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting SmartThings ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
            }

        @Override
        protected JSONObject doInBackground(String... args) {               //DevicesGet

            GetDevices jParser = new GetDevices();
            //SendParameter jParser = new SendParameter();

            // Name of you path (Smartapp)
            String path = "devices";

            JSONObject jsonDevices = jParser.getDevices(uriRequest,token,path);
            //JSONObject jsonDevices = jParser.sendParameter(uriRequest,token,path);

            return jsonDevices;
        }

        @Override
        protected void onPostExecute(JSONObject jsonDevices) {              //DevicesGet
            pDialog.dismiss();
            if (jsonDevices != null){

                try {
                    // List of elements to be retrieved by name (corresponding to the return of Smartapp)
                    String name = jsonDevices.getString("name");

                    Log.d("Uri Access", name);

                    auth.setText("Authenticated");
                    Access.setText("uri :" + name);

                    Log.i("json Devices: ", jsonDevices.toString());

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(getApplicationContext(), "Communication Request Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

}
