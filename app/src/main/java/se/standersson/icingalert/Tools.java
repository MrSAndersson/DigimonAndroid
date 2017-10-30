package se.standersson.icingalert;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

final class Tools {


    static void createExpandableListSummary(JSONObject data) throws JSONException {
        /*
         * Prepare status info into the hosts list
         */
        List<Host> hosts = new ArrayList<>();
        HashMap<String, Integer> hostPositions = new HashMap<>();


        // Check how many Hosts and Services are having trouble
        int servicesCount = data.getJSONArray("services").length();
        int hostsCount = data.getJSONArray("hosts").length();
        int hostsDownCount = data.getJSONObject("status").getInt("num_hosts_down");

        String hostName, hostComment, hostCommentAuthor, serviceName, serviceDetails, serviceComment, serviceCommentAuthor;
        int state, lastState;
        long lastStateChange;
        boolean notifications, hostAcknowledged, serviceAcknowledged;

        /*
            * Add all downed hosts to the list first in order to sort them to the top
             */
        for (int x = 0, y = 0; x < hostsCount; x++) {
            if (data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("state") == 1) {
                hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
                hostPositions.put(hostName, hosts.size());

                // Check for acknowledgements
                hostComment = "";
                hostCommentAuthor = "";
                if (data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("acknowledgement") != 0) {
                    hostAcknowledged = true;
                    for (int z = 0 ; z < data.getJSONArray("comments").length() ;  z++) {
                        if (data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("host_name").equals(hostName)
                                && data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("service_name").equals("")) {
                            hostComment = data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("text");
                            hostCommentAuthor = data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("author");
                        }
                    }
                } else {
                    hostAcknowledged = false;
                    hostComment = "";
                }



                hosts.add(new Host(hostName, true, hostAcknowledged, hostComment, hostCommentAuthor));
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
            serviceComment = "";
            serviceCommentAuthor = "";


            // Check for acknowledgements
            if (data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("acknowledgement") != 0) {
                serviceAcknowledged = true;

                for (int y = 0 ; y < data.getJSONArray("comments").length() ; y++) {
                    if (data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("host_name").equals(hostName)
                            && data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("service_name").equals(serviceName)) {
                        serviceComment = data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("text");
                        serviceCommentAuthor = data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("author");
                        Log.d("Hej", "Lol");
                    }
                }
            } else {
                serviceAcknowledged = false;
            }


            if (hostPositions.containsKey(hostName)) {
                hosts.get(hostPositions.get(hostName)).addService(serviceName, serviceDetails, state, lastState, lastStateChange, notifications, serviceAcknowledged, serviceComment, serviceCommentAuthor);
            } else {
                hostPositions.put(hostName, hosts.size());
                hosts.add(new Host(hostName, false, false, "", ""));
                hosts.get(hostPositions.get(hostName)).addService(serviceName, serviceDetails, state, lastState, lastStateChange, notifications, serviceAcknowledged, serviceComment, serviceCommentAuthor);
            }
        }

        // Sort all hosts and services in name order
        Collections.sort(hosts);
        for (Host host : hosts){
            host.sortServices();
        }

        // Save host info in HostSingleton;
        HostSingleton.getInstance().putHosts(hosts);
    }


    static List<HostList> filterProblems(List<Host> hosts){

        /*
        * Copy over downed hosts with all their services and all the hosts with troubled services
        */

        int hostCount = hosts.size();
        final List<HostList> hostList = new ArrayList<>();

        int addedCounter = 0;
        for (int x = 0 ; x < hostCount ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).isDown()){
                hostList.add(new HostList(x));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    hostList.get(hostList.size()-1).addService(y);
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceState(y) != 0){
                        try{
                            hostList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            hostList.add(new HostList(x));
                            hasBeenAdded = true;
                        }
                        hostList.get(hostList.size()-1).addService(y);
                    }
                }
            }
            if (hasBeenAdded) {
                addedCounter++;
            }
        }

        /*
         * Sort hosts according to being down and number of failing services
         */
        Collections.sort(hostList, new Comparator<HostList>() {
            @Override
            public int compare(HostList o1, HostList o2) {
                int warn = 1;
                int crit = 2;
                int unknown = 3;
                if(o1.isDown() && !o2.isDown()){
                    return -1;
                } else if (!o1.isDown() && o2.isDown()){
                    return 1;
                } else if(o1.getStateCount(crit) > o2.getStateCount(crit) ) {
                    return -1;
                } else if(o1.getStateCount(crit) < o2.getStateCount(crit)) {
                    return 1;
                } else if (o1.getStateCount(warn) > o2.getStateCount(warn)) {
                    return -1;
                } else if (o1.getStateCount(warn) < o2.getStateCount(warn)) {
                    return 1;
                } else if (o1.getStateCount(unknown) > o2.getStateCount(unknown)) {
                    return -1;
                } else if (o1.getStateCount(unknown) < o2.getStateCount(unknown)) {
                    return 1;
                } else {
                    return o1.getHostName().compareToIgnoreCase(o2.getHostName());
                }
            }
        });
        return hostList;
    }

    /*
     * Filter based on a string
     */
    static List<HostList> filterTextMatch(List<HostList> hosts, String searchString) {
        List<HostList> newHostList = new ArrayList<>();
        int addedCounter = 0;
        for (int x = 0 ; x < hosts.size() ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).getHostName().toLowerCase().contains(searchString.toLowerCase())) {
                newHostList.add(new HostList(hosts.get(x).getHost()));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    newHostList.get(newHostList.size()-1).addService(y);
                }
            } else {
                for (int y = 0; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceName(y).toLowerCase().contains(searchString.toLowerCase()) ||
                            hosts.get(x).getServiceDetails(y).toLowerCase().contains(searchString.toLowerCase())) {
                        try{
                            newHostList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newHostList.add(new HostList(hosts.get(x).getHost()));
                            hasBeenAdded = true;
                        }
                        newHostList.get(newHostList.size()-1).addService(y);
                    }
                }
            }
            // If we added this host: increase the counter
            if (hasBeenAdded) {
                addedCounter++;
            }
        }
        return newHostList;
    }

    // Create a HostList containing everything
    static List<HostList> fullHostList(List<Host> hosts) {
        List<HostList> newHostList = new ArrayList<>();

        for (int x=0 ; x < hosts.size() ; x++) {
            newHostList.add(new HostList(x));
            for (int y=0 ; y < HostSingleton.getInstance().getHosts().get(x).getServiceCount() ; y++) {
                newHostList.get(x).addService(y);
            }
        }

        return newHostList;
    }

    static String[] getLogin(Context context){
        //Get login credentials and make a call to get status data
        String[] prefsString = new String[3];
        SharedPreferences prefStorage = context.getSharedPreferences("Login", 0);
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");
        return prefsString;
    }

    /*
     * Check if device is connected to the network
     */
    static boolean isConnected(Context context){
        /*
        * Check Network Connectivity and then request data from Icinga
        * */

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}


