package se.standersson.icingalert;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.net.ssl.HttpsURLConnection;



class ServerInteraction {



    static String fetchData(final String[] prefs) throws Exception{

        /*
        * Try Catch to catch all errors in network communication
        * */
        JSONObject replyGroup = new JSONObject();
        String serverString = prefs[0];
        String username = prefs[1];
        String password = prefs[2];
        String statusURL = serverString + "/v1/status/CIB";
        String hostURL = serverString + "/v1/objects/hosts?attrs=last_check_result&attrs=state&attrs=name";
        String serviceURL = serverString + "/v1/objects/services?attrs=last_check_result&attrs=state&attrs=name&attrs=host_name";


        try {
            /*
            * Create a connection with input/output with a plaintext body
            * */
            String credentials = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
            replyGroup = sendRequest(replyGroup, "status", statusURL, credentials);
            replyGroup = sendRequest(replyGroup, "hosts", hostURL, credentials);
            replyGroup = sendRequest(replyGroup, "services", serviceURL, credentials);

            return replyGroup.toString();

            // Handle all known exceptions

        }catch (SocketTimeoutException e) {
            return "Connection Timed Out";
        } catch (MalformedURLException e) {
            return "Invalid URL";
        } catch (UnknownHostException e) {
            return "Resolve Failed";
        } catch (FileNotFoundException e) {
            return "FileNotFoundException";
        } catch (Exception e) {
            Log.e("NetworkException", e.toString());
            return "Unknown Exception";
        }
    }

    private static JSONObject sendRequest(JSONObject completeObject, String part, String serverString, String credentials) throws Exception {
        URL url = new URL(serverString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", credentials);
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setConnectTimeout(5000);
        connection.setDoInput(true);
        connection.setUseCaches(false);


        // Create the BufferedReader and add all batches together
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String json = "";
        String tmp;

        while ((tmp = reader.readLine()) != null) {
            json += tmp + "\n";
        }
        reader.close();
        connection.disconnect();
        if (part.equals("status")) {
            completeObject.put(part, new JSONObject(json).getJSONArray("results").getJSONObject(0).getJSONObject("status"));
        } else {
            completeObject.put(part, new JSONObject(json).getJSONArray("results"));
        }
        return completeObject;
    }

    static boolean checkReply(Context context, String reply){
        /*
        * Check reply for exceptions caught in communication
         */
        switch (reply) {
            case "Wrong credentials\n":
                Toast.makeText(context, "Wrong Credentials", Toast.LENGTH_LONG).show();
                return false;
            case "Connection Timed Out":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Invalid URL":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Resolve Failed":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "FileNotFoundException":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            case "Unknown Exception":
                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                return false;
            default:
                return true;
        }
    }

    static boolean isConnected(Context context){
        /*
        * Check Network Connectivity and then request data from Icinga
        * */

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
