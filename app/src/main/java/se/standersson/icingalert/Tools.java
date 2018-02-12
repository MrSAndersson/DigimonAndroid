package se.standersson.icingalert;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

final class Tools {


    static void parseData(JSONObject data) throws JSONException {
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
        boolean hostIsDown, hostAcknowledged, hostIsNotifying, serviceAcknowledged, serviceIsNotifying;

        /*
            * Add all downed hosts to the list first in order to sort them to the top
             */
        for (int x = 0, y = 0; x < hostsCount; x++) {
            hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
            hostIsNotifying = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getBoolean("enable_notifications");
            hostPositions.put(hostName, hosts.size());

            // Check for acknowledgements

            hostAcknowledged = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("acknowledgement") == 0 ? false : true;
            hostIsDown = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("state") == 0 ? false : true;


            // Check for host comments
            hostComment = "";
            hostCommentAuthor = "";
            for (int z = 0 ; z < data.getJSONArray("comments").length() ;  z++) {
                if (data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("host_name").equals(hostName)
                        && data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("service_name").equals("")) {
                    hostComment = data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("text");
                    hostCommentAuthor = data.getJSONArray("comments").getJSONObject(z).getJSONObject("attrs").getString("author");
                }
            }

            hosts.add(new Host(hostName, hostIsDown, hostAcknowledged, hostIsNotifying, hostComment, hostCommentAuthor));
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
            serviceIsNotifying = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getBoolean("enable_notifications");
            serviceComment = "";
            serviceCommentAuthor = "";


            // Check for acknowledgements
            serviceAcknowledged = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("acknowledgement") == 0 ? false : true;


            // Set comment
            for (int y = 0 ; y < data.getJSONArray("comments").length() ; y++) {
                if (data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("host_name").equals(hostName)
                        && data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("service_name").equals(serviceName)) {
                    serviceComment = data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("text");
                    serviceCommentAuthor = data.getJSONArray("comments").getJSONObject(y).getJSONObject("attrs").getString("author");
                }
            }

            hosts.get(hostPositions.get(hostName)).addService(serviceName, serviceDetails, state, lastState, lastStateChange, serviceIsNotifying, serviceAcknowledged, serviceComment, serviceCommentAuthor);
        }

        // Sort all hosts and services in name order
        Collections.sort(hosts);
        for (Host host : hosts){
            host.sortServices();
        }

        // Save host info in HostSingleton;
        HostSingleton.getInstance().putHosts(hosts);
    }


    static List<HostAbstract> filterProblems(List<Host> hosts){

        /*
        * Copy over downed hosts with all their services and all the hosts with troubled services
        */

        int hostCount = hosts.size();
        final List<HostAbstract> hostAbstract = new ArrayList<>();

        int addedCounter = 0;
        for (int x = 0 ; x < hostCount ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).isDown()){
                hostAbstract.add(new HostAbstract(x));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    hostAbstract.get(hostAbstract.size()-1).addService(y);
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceState(y) != 0){
                        try{
                            hostAbstract.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            hostAbstract.add(new HostAbstract(x));
                            hasBeenAdded = true;
                        }
                        hostAbstract.get(hostAbstract.size()-1).addService(y);
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
        Collections.sort(hostAbstract, new Comparator<HostAbstract>() {
            @Override
            public int compare(HostAbstract o1, HostAbstract o2) {
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
        return hostAbstract;
    }

    /*
     * Filter based on a string
     */
    static List<HostAbstract> filterTextMatch(List<HostAbstract> hosts, String searchString) {
        List<HostAbstract> newHostAbstract = new ArrayList<>();
        int addedCounter = 0;
        for (int x = 0 ; x < hosts.size() ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).getHostName().toLowerCase().contains(searchString.toLowerCase())) {
                newHostAbstract.add(new HostAbstract(hosts.get(x).getHost()));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    newHostAbstract.get(newHostAbstract.size()-1).addService(y);
                }
            } else {
                for (int y = 0; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceName(y).toLowerCase().contains(searchString.toLowerCase()) ||
                            hosts.get(x).getServiceDetails(y).toLowerCase().contains(searchString.toLowerCase())) {
                        try{
                            newHostAbstract.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newHostAbstract.add(new HostAbstract(hosts.get(x).getHost()));
                            hasBeenAdded = true;
                        }
                        newHostAbstract.get(newHostAbstract.size()-1).addService(y);
                    }
                }
            }
            // If we added this host: increase the counter
            if (hasBeenAdded) {
                addedCounter++;
            }
        }
        return newHostAbstract;
    }

    // Create a HostAbstract containing everything
    static List<HostAbstract> fullHostList(List<Host> hosts) {
        List<HostAbstract> newHostAbstract = new ArrayList<>();

        for (int x=0 ; x < hosts.size() ; x++) {
            newHostAbstract.add(new HostAbstract(x));
            for (int y=0 ; y < HostSingleton.getInstance().getHosts().get(x).getServiceCount() ; y++) {
                newHostAbstract.get(x).addService(y);
            }
        }

        return newHostAbstract;
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


