package se.standersson.icingalert;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Host implements Serializable, Comparable<Host>{
    /*
    * Declaration of host details
     */
    private final String hostName;
    private final List<Service> services = new ArrayList<>();
    private final List<Integer> okList = new ArrayList<>();
    private final List<Integer> critList = new ArrayList<>();
    private final List<Integer> warnList = new ArrayList<>();
    private final List<Integer> unknownList = new ArrayList<>();
    private boolean isDown = false;
    private final boolean acknowledged;
    private final String comment;


    //Constructor for hosts
    Host(String hostName, boolean isDown, boolean acknowledged, String comment) {
        this.hostName = hostName;
        this.acknowledged = acknowledged;
        this.comment = comment;
        if (isDown) {
            this.isDown = true;
        }
    }

    //Add a service with name, details and state. Also increment the state counters
    void addService(String serviceName, String serviceDetails, int state, int lastState, long lastStateChange, boolean notifications, boolean acknowledged, String comment){
        services.add(new Service(serviceName, serviceDetails, state, lastState, lastStateChange, notifications, acknowledged, comment));
        switch (state){
            case 0:
                okList.add(services.size()-1);
                break;
            case 1:
                warnList.add(services.size()-1);
                break;
            case 2:
                critList.add(services.size()-1);
                break;
            case 3:
                unknownList.add(services.size()-1);
                break;
        }
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
        return services.size();
    }

    String getHostName(){
        return hostName;
    }

    boolean isServiceExpanded(int position) {
        return services.get(position).isExpanded();
    }

    void setServiceExpanded(int position, boolean expanded) {
        services.get(position).setIsExpanded(expanded);
    }

    void setServiceNotifying(int position, boolean isNotifying) {
        services.get(position).setIsNotifying(isNotifying);
    }

    int getServiceLastState(int position) {
        return services.get(position).getLastState();
    }

    long getServiceLastStateChange(int position) {
        return services.get(position).getLastStateChange();
    }

    boolean isServiceNotifying(int position) {
        return services.get(position).isNotifying();
    }

    boolean isServiceAcknowledged(int position) {
        return services.get(position).isAcknowledged();
    }

    String getServiceComment(int position) {
        return services.get(position).getComment();
    }

    int findServiceName(String serviceName) {
        for ( int x=0 ; x<services.size() ; x++) {
            if (services.get(x).getServiceName().equals(serviceName)) {
                return x;
            }
        }
        return -1;
    }


    int getStateCount(int state){
        switch (state) {
            case 0:
                return okList.size();
            case 1:
                return warnList.size();
            case 2:
                return critList.size();
            case 3:
                return unknownList.size();
            default:
                // Return 0 so we won't try to draw something we don't have
                return 0;
        }
    }

    boolean isDown(){
        return isDown;
    }

    boolean isAcknowledged() {
        return acknowledged;
    }

    String getComment() {
        return comment;
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
        private final int last_state;
        private final long last_state_change;
        private boolean isExpanded=false;
        private boolean notifications;
        private final boolean acknowledged;
        private final String comment;


        Service(String name, String details, int state, int last_state, long last_state_change, boolean notifications, boolean acknowledged, String comment) {
            this.name = name;
            this.details = details;
            this.state = state;
            this.last_state = last_state;
            this.last_state_change = last_state_change;
            this.notifications = notifications;
            this.acknowledged = acknowledged;
            this.comment = comment;
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

        int getLastState(){
            return last_state;
        }

        long getLastStateChange(){
            return last_state_change;
        }

        boolean isNotifying() {
            return notifications;
        }

        boolean isExpanded() {
            return isExpanded;
        }

        void setIsExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        void setIsNotifying(boolean notifications) {
            this.notifications = notifications;
        }

        boolean isAcknowledged() {
            return acknowledged;
        }

        String getComment() {
            return comment;
        }

        @Override
        public int compareTo(@NonNull Service other) {
            return this.getServiceName().compareToIgnoreCase(other.getServiceName());
        }
    }
}
