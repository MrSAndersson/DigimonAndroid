package se.standersson.icingalert;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class Tools {

    static List<Host> filterProblems(List<Host> hosts){

        /*
        * Copy over downed hosts with all their services and all the hosts with troubled services
        */

        int hostCount = hosts.size();
        List<Host> newList = new ArrayList<>();
        int addedCounter = 0;
        for (int x = 0 ; x < hostCount ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).isDown()){
                newList.add(new Host(hosts.get(x).getHostName(), true));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    newList.get(newList.size()-1).addService(hosts.get(x).getServiceName(y), hosts.get(x).getServiceDetails(y), hosts.get(x).getServiceState(y), hosts.get(x).getServiceLastState(y), hosts.get(x).getServiceLastStateChange(y), hosts.get(x).isServiceNotifying(y));
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceState(y) != 0){
                        try{
                            newList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newList.add(new Host(hosts.get(x).getHostName(), false));
                            hasBeenAdded = true;
                        }
                        newList.get(newList.size()-1).addService(hosts.get(x).getServiceName(y), hosts.get(x).getServiceDetails(y), hosts.get(x).getServiceState(y), hosts.get(x).getServiceLastState(y), hosts.get(x).getServiceLastStateChange(y), hosts.get(x).isServiceNotifying(y));
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
        Collections.sort(newList, new Comparator<Host>() {
            @Override
            public int compare(Host o1, Host o2) {
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
        return newList;
    }

    /*
     * Filter based on a string
     */
    static List<Host> filterTextMatch(List<Host> hosts, String searchString) {
        List<Host> newList = new ArrayList<>();
        int addedCounter = 0;
        for (int x = 0 ; x < hosts.size() ; x++) {
            boolean hasBeenAdded = false;
            if (hosts.get(x).getHostName().toLowerCase().contains(searchString.toLowerCase())) {
                newList.add(new Host(hosts.get(x).getHostName(), hosts.get(x).isDown()));
                hasBeenAdded = true;
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    newList.get(newList.size()-1).addService(hosts.get(x).getServiceName(y), hosts.get(x).getServiceDetails(y), hosts.get(x).getServiceState(y), hosts.get(x).getServiceLastState(y), hosts.get(x).getServiceLastStateChange(y), hosts.get(x).isServiceNotifying(y));
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    if (hosts.get(x).getServiceName(y).toLowerCase().contains(searchString.toLowerCase()) ||
                            hosts.get(x).getServiceDetails(y).toLowerCase().contains(searchString.toLowerCase())) {
                        try{
                            newList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newList.add(new Host(hosts.get(x).getHostName(), hosts.get(x).isDown()));
                            hasBeenAdded = true;
                        }
                        newList.get(newList.size()-1).addService(hosts.get(x).getServiceName(y), hosts.get(x).getServiceDetails(y), hosts.get(x).getServiceState(y), hosts.get(x).getServiceLastState(y), hosts.get(x).getServiceLastStateChange(y), hosts.get(x).isServiceNotifying(y));
                    }
                }
            }
            // If we added this host: increase the counter
            if (hasBeenAdded) {
                addedCounter++;
            }
        }
        return newList;
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
}
