package se.standersson.icingalert;

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

    //Constructor for hosts that aren't down currently
    Host(String hostName){
        this.hostName = hostName;
        this.jsonHostPosition = null;
        stateCounter.put(1,0);
        stateCounter.put(2,0);
        stateCounter.put(3,0);
    }

    //Constructor for hosts that are down currently
    Host(String hostName, int jsonHostPosition) {
        this.hostName = hostName;
        this.jsonHostPosition = jsonHostPosition;
        this.isDown = true;
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
        stateCounter.put(state, current+1);
    }

    // Get the JSON position of a service
    int getServicePosition(int childPosition) {
        return services.get(childPosition);
    }


    String getServiceName(int childPosition) {
        return serviceNames.get(services.get(childPosition));
    }

    String getServiceDetails(int childPosition) {
        return serviceDetails.get(services.get(childPosition));
    }

    int getServiceState(int childPosition) {
        return serviceState.get(services.get(childPosition));
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
