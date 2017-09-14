package se.standersson.icingalert;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Map of a subset of host/services and pointers to their place in HostSingleton
 */

class HostList implements Serializable, Comparable<HostList> {
    private final int hostPosition;
    private final List<Integer> service = new ArrayList<>();

    HostList(int hostPosition) {
        this.hostPosition = hostPosition;
    }

    void addService(int position) {
        service.add(service.size(), position);
    }

    int getHost() {
        return hostPosition;
    }


    int getServiceCount() {
        return service.size();
    }

    String getHostName() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getHostName();
    }

    String getComment() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getComment();
    }

    String getCommentAuthor() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getCommentAuthor();
    }

    boolean isDown() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isDown();
    }

    boolean isAcknowledged() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isAcknowledged();
    }

    boolean isExpanded() {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isExpanded();
    }

    void setExpanded(boolean expanded) {
        HostSingleton.getInstance().getHosts().get(hostPosition).setExpanded(expanded);
    }

    int getStateCount(int state) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getStateCount(state);
    }

    int getStateAckCount(int state) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getStateAckCount(state);
    }

    String getServiceName(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceName(service.get(servicePosition));
    }

    String getServiceDetails(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceDetails(service.get(servicePosition));
    }

    int getServiceState(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceState(service.get(servicePosition));
    }

    String getServiceComment(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceComment(service.get(servicePosition));
    }

    String getServiceCommentAuthor(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceCommentAuthor(servicePosition);
    }

    long getServiceLastStateChange(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).getServiceLastStateChange(service.get(servicePosition));
    }

    boolean isServiceAcknowledged(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isServiceAcknowledged(service.get(servicePosition));
    }

    boolean isServiceNotifying(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isServiceNotifying(service.get(servicePosition));
    }

    boolean isServiceExpanded(int servicePosition) {
        return HostSingleton.getInstance().getHosts().get(hostPosition).isServiceExpanded(service.get(servicePosition));
    }

    void setServiceNotifying(int servicePosition, boolean isNotifying) {
        HostSingleton.getInstance().getHosts().get(hostPosition).setServiceNotifying(service.get(servicePosition), isNotifying);
    }

    void setServiceExpanded(int servicePosition, boolean expanded) {
        HostSingleton.getInstance().getHosts().get(hostPosition).setServiceExpanded(service.get(servicePosition), expanded);
    }

    /*
    * Sorts hosts in alphabetical order
     */

    @Override
    public int compareTo(@NonNull HostList other) {
        return this.getHostName().compareToIgnoreCase(
                other.getHostName());
    }
}

class Host implements Serializable, Comparable<Host>{
    /*
    * Declaration of host details
     */
    private final String hostName;
    private final List<Service> services = new ArrayList<>();
    private final List<Integer> okList = new ArrayList<>();
    private final List<Integer> critList = new ArrayList<>();
    private final List<Integer> critAckList = new ArrayList<>();
    private final List<Integer> warnList = new ArrayList<>();
    private final List<Integer> warnAckList = new ArrayList<>();
    private final List<Integer> unknownList = new ArrayList<>();
    private final List<Integer> unknownAckList = new ArrayList<>();
    private boolean isDown = false;
    private boolean isExpanded = false;
    private final boolean acknowledged;
    private final String comment;
    private final String commentAuthor;


    //Constructor for hosts
    Host(String hostName, boolean isDown, boolean acknowledged, String comment, String commentAuthor) {
        this.hostName = hostName;
        this.acknowledged = acknowledged;
        this.comment = comment;
        this.commentAuthor = commentAuthor;
        if (isDown) {
            this.isDown = true;
        }
    }

    //Add a service with name, details and state. Also increment the state counters
    void addService(String serviceName, String serviceDetails, int state, int lastState, long lastStateChange, boolean notifications, boolean acknowledged, String comment, String commentAuthor){
        services.add(new Service(serviceName, serviceDetails, state, lastState, lastStateChange, notifications, acknowledged, comment, commentAuthor));
        switch (state){
            case 0:
                okList.add(services.size() - 1);
                break;
            case 1:
                if (acknowledged) {
                    warnAckList.add(services.size()-1);
                } else {
                    warnList.add(services.size() - 1);
                }
                break;
            case 2:
                if (acknowledged) {
                    critAckList.add(services.size()-1);
                } else {
                    critList.add(services.size() - 1);
                }
                break;
            case 3:
                if (acknowledged) {
                    unknownAckList.add(services.size()-1);
                } else {
                    unknownList.add(services.size() - 1);
                }
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

    void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    boolean isExpanded() {
        return this.isExpanded;
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

    String getServiceCommentAuthor(int position) {
        return services.get(position).getCommentAuthor();
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

    int getStateAckCount(int state){
        switch (state) {
            case 0:
                return okList.size();
            case 1:
                return warnAckList.size();
            case 2:
                return critAckList.size();
            case 3:
                return unknownAckList.size();
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

    String getCommentAuthor() {
        return commentAuthor;
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
        private final String commentAuthor;


        Service(String name, String details, int state, int last_state, long last_state_change, boolean notifications, boolean acknowledged, String comment, String commentAuthor) {
            this.name = name;
            this.details = details;
            this.state = state;
            this.last_state = last_state;
            this.last_state_change = last_state_change;
            this.notifications = notifications;
            this.acknowledged = acknowledged;
            this.comment = comment;
            this.commentAuthor = commentAuthor;
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

        String getCommentAuthor() {
            return commentAuthor;
        }

        @Override
        public int compareTo(@NonNull Service other) {
            return this.getServiceName().compareToIgnoreCase(other.getServiceName());
        }
    }
}
