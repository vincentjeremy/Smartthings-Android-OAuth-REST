# Smartthings-Android-OAuth-REST
Example to communique with an Android App with Smartthings OAuth2 protocole


oauth2-server

  This project is an implementation for OAuth 2.0 Specification. Especially, the protocol of the server area is covered by this project.

Current supported Grant types

  - Connection with Smatthings website for autorization
  - Get Token
  - Get Uri

  - Get data from Smartapp
  - Send Data to Smartapp

Current supported token types

  - Bearer

How to use

  This project is working on the authentication process by OAuth2 with the Smartthings platform.
  To use this project, you must provide the recover id on your smartapp:
        
        private static String CLIENT_ID = "YOUR-CLIENT_ID";
        // Use your own client id
        private static String CLIENT_SECRET ="YOUR-CLIENT_SECRET";
        // Use your own client secret

  After the application runs all by itself to recover the Token (valid about 50 years), the Uri and Url of EndPoint.

  This project pocède two examples of communication with a Smartapp:

    - Recover data
        GetDevices jParser = new GetDevices();
        JSONObject jsonDevices = jParser.getDevices(uriRequest,token,path);
    - Send data (and receive the answer that everything is well spent)
        SendParameter jParser = new SendParameter();
        JSONObject jsonDevices = jParser.sendParameter(uriRequest,token,path);  

  Attention, always use it's command, as shown in the project, assynchronously
