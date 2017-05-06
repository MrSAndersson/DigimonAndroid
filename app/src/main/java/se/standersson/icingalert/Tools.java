package se.standersson.icingalert;


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
                    int serviceID = hosts.get(x).getServiceID(y);
                    newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    int serviceID = hosts.get(x).getServiceID(y);
                    if (hosts.get(x).getServiceState(serviceID) != 0){
                        try{
                            newList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newList.add(new Host(hosts.get(x).getHostName(), false));
                            hasBeenAdded = true;
                        }
                        newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
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
                    int serviceID = hosts.get(x).getServiceID(y);
                    newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
                }
            } else {
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++) {
                    int serviceID = hosts.get(x).getServiceID(y);
                    if (hosts.get(x).getServiceName(serviceID).toLowerCase().contains(searchString.toLowerCase()) ||
                            hosts.get(x).getServiceDetails(serviceID).toLowerCase().contains(searchString.toLowerCase())) {
                        try{
                            newList.get(addedCounter);
                        } catch (IndexOutOfBoundsException e) {
                            newList.add(new Host(hosts.get(x).getHostName(), hosts.get(x).isDown()));
                            hasBeenAdded = true;
                        }
                        newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
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
}
