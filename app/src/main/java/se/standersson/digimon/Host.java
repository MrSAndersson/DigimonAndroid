package se.standersson.digimon;

import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 2017-02-18.
 */

class Host {
    /*
    * Declaration of host details
     */
    private String hostName;
    private List<Integer> services;
    private SparseArray<String> serviceNames;
    private SparseArray<String> serviceDetails;
    private SparseIntArray serviceState;
    private Integer jsonHostPosition;
    private SparseArray<Integer> stateCounter;



    Host(String hostName){
        this.hostName = hostName;

        this.stateCounter = new SparseArray<>();
        this.jsonHostPosition = null;
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);

        this.services = new ArrayList<>();
        this.serviceNames = new SparseArray<>();
        this.serviceDetails = new SparseArray<>();
        this.serviceState = new SparseIntArray();


    }

    Host(String hostName, int jsonHostPosition) {
        this.hostName = hostName;
        this.stateCounter = new SparseArray<>();
        this.jsonHostPosition = jsonHostPosition;
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);


        this.services = new ArrayList<>();
        this.serviceNames = new SparseArray<>();
        this.serviceDetails = new SparseArray<>();
        this.serviceState = new SparseIntArray();
    }

    void addService(int jsonPosition, String serviceName, String serviceDetails, int state){
        services.add(jsonPosition);
        this.serviceNames.put(jsonPosition, serviceName);
        this.serviceDetails.put(jsonPosition, serviceDetails);
        this.serviceState.put(jsonPosition, state);
        int current = stateCounter.get(state);
        stateCounter.put(state, current+1);
    }

    int getServicePosition(int childNumber) {
        return services.get(childNumber);
    }

    String getServiceName(int childNumber) {
        return serviceNames.get(services.get(childNumber));
    }

    String getServiceDetails(int childNumber) {
        return serviceDetails.get(services.get(childNumber));
    }

    int getServiceCount(){
        return services.size();
    }

    String getHostName(){
        return hostName;
    }

    int getStateCount(int state){
        return stateCounter.get(state);
    }

    boolean hasService(int service){
        return services.contains(service);
    }

    boolean isDown(){
        if (jsonHostPosition == null){
            return false;
        } else {
            return true;
        }
    }
    int jsonHostPosition(){
        return jsonHostPosition;
    }
}
