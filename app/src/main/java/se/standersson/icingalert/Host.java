package se.standersson.icingalert;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Host implements Serializable{
    /*
    * Declaration of host details
     */
    private String hostName;
    private List<Integer> services = new ArrayList<>();
    private List<Integer> critList = new ArrayList<>();
    private List<Integer> warnList = new ArrayList<>();
    private List<Integer> unknownList = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, String> serviceNames = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, String> serviceDetails = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Integer> serviceState = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Integer> stateCounter = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private Integer jsonHostPosition;
    private boolean isDown = false;



    //Constructor for hosts that aren't down currently
    Host(String hostName){
        this.hostName = hostName;
        this.jsonHostPosition = null;
        stateCounter.put(0,0);
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);
    }

    //Constructor for hosts that are down currently
    Host(String hostName, int jsonHostPosition) {
        this.hostName = hostName;
        this.jsonHostPosition = jsonHostPosition;
        this.isDown = true;
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
    int getServicePosition(int childPosition, boolean isTroubleList) {
        if (isTroubleList){
            if (childPosition < stateCounter.get(2)){
                return critList.get(childPosition);
            } else if (childPosition - stateCounter.get(2) < stateCounter.get(1)){
                return warnList.get(childPosition - stateCounter.get(2));
            } else {
                return unknownList.get(childPosition - stateCounter.get(2) -stateCounter.get(1));
            }
        } else {
            return services.get(childPosition);
        }
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

    int getServiceCount(boolean notOk){
        int counter;
        if (notOk){
            counter = stateCounter.get(1) + stateCounter.get(2) + stateCounter.get(3);
        } else {
            counter = stateCounter.get(0) + stateCounter.get(1) + stateCounter.get(2) + stateCounter.get(3);
        }
        return counter;
    }

    String getHostName(){
        return hostName;
    }

    int getStateCount(int state){
        return stateCounter.get(state);
    }

    // Check if the host has a particular service (JSON position)
    boolean hasService(int service){
        return services.contains(service);
    }

    boolean isDown(){
        return isDown;
    }
    int jsonHostPosition(){
        return jsonHostPosition;
    }
}
