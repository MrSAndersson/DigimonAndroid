package se.standersson.icingalert;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;



class ServerInteraction {



    static String fetchData(Context context, final String[] prefs){

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

            /*
            * Create a connection with input/output with a plaintext body
            * */
        String credentials = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);

        try {
            replyGroup = sendRequest(replyGroup, "status", statusURL, credentials);
            replyGroup = sendRequest(replyGroup, "hosts", hostURL, credentials);
            replyGroup = sendRequest(replyGroup, "services", serviceURL, credentials);

        }catch (SocketTimeoutException e) {
            Toast.makeText(context, "Connection Timed Out", Toast.LENGTH_LONG).show();
            return null;
        } catch (MalformedURLException e) {
            Toast.makeText(context, "Invalid URL", Toast.LENGTH_LONG).show();
            return null;
        } catch (UnknownHostException e) {
            Toast.makeText(context, "Resolve Failed", Toast.LENGTH_LONG).show();
            return null;
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "FileNotFoundException", Toast.LENGTH_LONG).show();
            return null;
        } catch (Exception e) {
            Log.e("NetworkException", e.toString());
            Toast.makeText(context, "Unknown Exception", Toast.LENGTH_LONG).show();
            return null;
        }
        return replyGroup.toString();

    }

    private static JSONObject sendRequest(JSONObject completeObject, String part, String serverString, String credentials) throws Exception {
        URL url = new URL(serverString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", credentials);
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setConnectTimeout(10000);
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

    static List<Host> createExpandableListSummary(String reply) throws JSONException {
        /*
         * Prepare status info into the hosts list
         */
        JSONObject data = new JSONObject(reply);
        List<Host> hosts = new ArrayList<>();
        HashMap<String, Integer> hostPositions = new HashMap<>();


        // Check how many Hosts and Services are having trouble
        int servicesCount = data.getJSONArray("services").length();
        int hostsCount = data.getJSONArray("hosts").length();
        int hostsDownCount = data.getJSONObject("status").getInt("num_hosts_down");

        String hostName, serviceName;
        String serviceDetails;
        int state;

        /*
            * Add all downed hosts to the list first in order to sort them to the top
             */
        for (int x = 0, y = 0; x < hostsCount; x++) {
            if (data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("state") == 1) {
                hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
                hostPositions.put(hostName, hosts.size());
                hosts.add(new Host(hostName, true));
                y++;
                if (y == hostsDownCount) {
                    break;
                }
            }
        }

            /*
            * Loop through all services and store the hostname and the location of their respective services
             */
        for (int x=0 ; x < servicesCount ; x++) {
            hostName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("host_name");
            serviceName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("name");
            serviceDetails = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getJSONObject("last_check_result").getString("output");
            state = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("state");

            if (hostPositions.containsKey(hostName)) {
                hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
            } else {
                hostPositions.put(hostName, hosts.size());
                hosts.add(new Host(hostName, false));
                hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
            }
        }

        // Resort into alphabetical order with Downed and trouble hosts at the top
        Collections.sort(hosts);

        return hosts;
    }

}
