package se.standersson.icingalert;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class Tools {



    static List<HostList> filterProblems(List<Host> hosts){

        /*
        * Copy over downed hosts with all their services and all the hosts with troubled services
        */

        int hostCount = hosts.size();
        List<HostList> hostList = new ArrayList<>();

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
                if(HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).isDown() && !HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).isDown()){
                    return -1;
                } else if (!HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).isDown() && HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).isDown()){
                    return 1;
                } else if(HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(crit) > HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(crit) ) {
                    return -1;
                } else if(HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(crit) < HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(crit)) {
                    return 1;
                } else if (HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(warn) > HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(warn)) {
                    return -1;
                } else if (HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(warn) < HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(warn)) {
                    return 1;
                } else if (HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(unknown) > HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(unknown)) {
                    return -1;
                } else if (HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getStateCount(unknown) < HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getStateCount(unknown)) {
                    return 1;
                } else {
                    return HostSingleton.getInstance().getHosts().get(o1.getHostPosition()).getHostName().compareToIgnoreCase(HostSingleton.getInstance().getHosts().get(o2.getHostPosition()).getHostName());
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
            if (HostSingleton.getInstance().getHosts().get(hosts.get(x).getHostPosition()).getHostName().toLowerCase().contains(searchString.toLowerCase())) {
                newHostList.add(new HostList(hosts.get(x).getHostPosition()));
                hasBeenAdded = true;
                for (int y = 0 ; y < HostSingleton.getInstance().getHosts().get(x).getServiceCount() ; y++) {
                    newHostList.get(newHostList.size()-1).addService(y);
                }
            } else {
                for (int y = 0 ; y < HostSingleton.getInstance().getHosts().get(hosts.get(x).getHostPosition()).getServiceCount() ; y++) {
                    if (HostSingleton.getInstance().getHosts().get(hosts.get(x).getHostPosition()).getServiceName(y).toLowerCase().contains(searchString.toLowerCase()) ||
                            HostSingleton.getInstance().getHosts().get(hosts.get(x).getHostPosition()).getServiceDetails(y).toLowerCase().contains(searchString.toLowerCase())) {
                        try{
                            newHostList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newHostList.add(new HostList(hosts.get(x).getHostPosition()));
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

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
