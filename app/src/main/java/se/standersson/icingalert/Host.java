package se.standersson.icingalert;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Host implements Serializable, Comparable<Host>{
    /*
    * Declaration of host details
     */
    private final String hostName;
    private final List<Integer> services = new ArrayList<>();
    private final List<Integer> critList = new ArrayList<>();
    private final List<Integer> warnList = new ArrayList<>();
    private final List<Integer> unknownList = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, String> serviceNames = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, String> serviceDetails = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Integer> serviceState = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Integer> stateCounter = new HashMap<>();
    private boolean isDown = false;


    //Constructor for hosts that are down currently
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
    void addService(int jsonPosition, String serviceName, String serviceDetails, int state){
        services.add(jsonPosition);
        this.serviceNames.put(jsonPosition, serviceName);
        this.serviceDetails.put(jsonPosition, serviceDetails);
        this.serviceState.put(jsonPosition, state);
        int current = stateCounter.get(state);
        switch (state){
            case 1:
                warnList.add(jsonPosition);
                break;
            case 2:
                critList.add(jsonPosition);
                break;
            case 3:
                unknownList.add(jsonPosition);
                break;
        }
        stateCounter.put(state, current+1);
    }

    // Get the JSON position of a service
    int getServicePosition(int childPosition) {
            return services.get(childPosition);
    }


    String getServiceName(int servicePosition) {
        return serviceNames.get(servicePosition);
    }

    String getServiceDetails(int servicePosition) {
        return serviceDetails.get(servicePosition);
    }

    int getServiceState(int servicePosition) {
        return serviceState.get(servicePosition);
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

    /*
    * Sort hosts that are down first, then trouble services and everything else in alphabetical order
     */

    @Override
    public int compareTo(@NonNull Host other) {
        /*if(this.isDown() && !other.isDown()){
            return -1;
        } else if (!this.isDown() && other.isDown()){
            return 1;
        } else if(this.getServiceCount() != 0 && other.getServiceCount() == 0 ) {
            return -1;
        } else if(this.getServiceCount() == 0 && other.getServiceCount() != 0) {
            return 1;
        }else {*/
            return this.getHostName().compareToIgnoreCase(other.getHostName());
       // }
    }
}
