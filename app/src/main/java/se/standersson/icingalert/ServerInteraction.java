package se.standersson.icingalert;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;



class ServerInteraction {



    static String fetchData(final String[] prefs)throws  Exception{

        /*
        * Try Catch to catch all errors in network communication
        * */
        JSONObject replyGroup = new JSONObject();
        String serverString = prefs[0];
        String username = prefs[1];
        String password = prefs[2];
        String statusURL = serverString + "/v1/status/CIB";
        String hostURL = serverString + "/v1/objects/hosts?attrs=last_check_result&attrs=state&attrs=name";
        String serviceURL = serverString + "/v1/objects/services?attrs=last_check_result&attrs=state&attrs=name&attrs=host_name&attrs=last_state&attrs=last_state_change&attrs=enable_notifications";

            /*
            * Create a connection with input/output with a plaintext body
            * */
        String credentials = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);

            replyGroup = sendStatusRequest(replyGroup, "status", statusURL, credentials);
            replyGroup = sendStatusRequest(replyGroup, "hosts", hostURL, credentials);
            replyGroup = sendStatusRequest(replyGroup, "services", serviceURL, credentials);
        return replyGroup.toString();

    }

    private static JSONObject sendStatusRequest(JSONObject completeObject, String part, String serverString, String credentials) throws Exception {
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

        String hostName, serviceName, serviceDetails;
        int state, lastState;
        long lastStateChange;
        boolean notifications;

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
            lastState = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("last_state");
            lastStateChange = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getLong("last_state_change");
            notifications = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getBoolean("enable_notifications");



            if (hostPositions.containsKey(hostName)) {
                hosts.get(hostPositions.get(hostName)).addService(serviceName, serviceDetails, state, lastState, lastStateChange, notifications);
            } else {
                hostPositions.put(hostName, hosts.size());
                hosts.add(new Host(hostName, false));
                hosts.get(hostPositions.get(hostName)).addService(serviceName, serviceDetails, state, lastState, lastStateChange, notifications);
            }
        }

        // Sort all hosts and services in name order
        Collections.sort(hosts);
        for (Host host : hosts){
            host.sortServices();
        }

        return hosts;
    }

    /*
     * Send an Action to Icinga
     */



    /*
     * Check if device is connected to the network
     */
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
