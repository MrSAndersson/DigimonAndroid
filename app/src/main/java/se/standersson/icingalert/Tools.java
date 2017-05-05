package se.standersson.icingalert;


import java.util.ArrayList;
import java.util.List;

final class Tools {

    static List<Host> filterProblems(List<Host> hosts){
        int hostCount = hosts.size();
        List<Host> newList = new ArrayList<>();

        for (int x = 0 ; x < hostCount ; x++){
            if (hosts.get(x).isDown()){
                newList.add(new Host(hosts.get(x).getHostName(), true));
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++){
                    int serviceID = hosts.get(x).getServicePosition(y);
                    newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
                }
            } else{
                for (int y = 0 ; y < hosts.get(x).getServiceCount() ; y++){
                    int serviceID = hosts.get(x).getServicePosition(y);
                    if (hosts.get(x).getServiceState(serviceID) != 0){
                        try{
                            newList.get(x);
                        } catch (IndexOutOfBoundsException e){
                            newList.add(new Host(hosts.get(x).getHostName(), false));
                        }
                        newList.get(newList.size()-1).addService(y, hosts.get(x).getServiceName(serviceID), hosts.get(x).getServiceDetails(serviceID), hosts.get(x).getServiceState(serviceID));
                    }
                }
            }
        }
        return newList;
    }
}
