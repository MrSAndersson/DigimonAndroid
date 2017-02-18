package se.standersson.digimon;

import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

class Host {
    /*
    * Declaration of host details
     */
    private String hostName;
    private List<Integer> services = new ArrayList<>();
    private SparseArray<String> serviceNames = new SparseArray<>();
    private SparseArray<String> serviceDetails = new SparseArray<>();
    private SparseIntArray serviceState = new SparseIntArray();
    private Integer jsonHostPosition;
    private boolean isDown = false;
    private SparseIntArray stateCounter = new SparseIntArray();



    Host(String hostName){
        this.hostName = hostName;
        this.jsonHostPosition = null;
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);
    }

    Host(String hostName, int jsonHostPosition) {
        this.hostName = hostName;
        this.jsonHostPosition = jsonHostPosition;
        this.isDown = true;
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);
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
        return isDown;
    }
    int jsonHostPosition(){
        return jsonHostPosition;
    }
}
