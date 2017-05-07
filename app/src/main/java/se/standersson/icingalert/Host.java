package se.standersson.icingalert;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class Host implements Serializable, Comparable<Host>{
    /*
    * Declaration of host details
     */
    private final String hostName;
    private final List<Service> services = new ArrayList<>();
    private final List<Integer> critList = new ArrayList<>();
    private final List<Integer> warnList = new ArrayList<>();
    private final List<Integer> unknownList = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Integer> stateCounter = new HashMap<>();
    private boolean isDown = false;


    //Constructor for hosts
    Host(String hostName, boolean isDown) {
        this.hostName = hostName;
        if (isDown) {
            this.isDown = true;
        }
        stateCounter.put(0,0);
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);
    }

    //Add a service with name, details and state. Also increment the state counters
    void addService(String serviceName, String serviceDetails, int state){
        int current = stateCounter.get(state);
        switch (state){
            case 1:
                warnList.add(services.size());
                break;
            case 2:
                critList.add(services.size());
                break;
            case 3:
                unknownList.add(services.size());
                break;
        }
        stateCounter.put(state, current+1);
        services.add(new Service(serviceName, serviceDetails, state));
    }

    String getServiceName(int servicePosition) {
        return services.get(servicePosition).getServiceName();
    }

    String getServiceDetails(int servicePosition) {
        return services.get(servicePosition).getDetails();
    }

    int getServiceState(int servicePosition) {
        return services.get(servicePosition).getState();
    }

    int getServiceCount(){
        return stateCounter.get(0) + stateCounter.get(1) + stateCounter.get(2) + stateCounter.get(3);
    }

    String getHostName(){
        return hostName;
    }

    int getStateCount(int state){
        return stateCounter.get(state);
    }

    boolean isDown(){
        return isDown;
    }

    void sortServices(){
        Collections.sort(services);
    }

    /*
    * Sorts hosts in alphabetical order
     */

    @Override
    public int compareTo(@NonNull Host other) {
            return this.getHostName().compareToIgnoreCase(other.getHostName());
    }

    class Service implements Serializable, Comparable<Service> {
        private final String name;
        private final String details;
        private final int state;


        Service(String name, String details, int state) {
            this.name = name;
            this.details = details;
            this.state = state;
        }

        String getServiceName() {
            return name;
        }

        String getDetails() {
            return details;
        }

        int getState() {
            return state;
        }

        @Override
        public int compareTo(@NonNull Service other) {
            return this.getServiceName().compareToIgnoreCase(other.getServiceName());
        }
    }
}
