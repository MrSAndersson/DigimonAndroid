package se.standersson.icingalert;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


final class Tools {



    static Bundle createExpandableListSummary(JSONObject data) throws JSONException{
        /*
         * Prepare status info into the hosts list
         */
        List<Host> hosts = new ArrayList<>();
        int hostListCount;
        HashMap<String, Integer> hostPositions = new HashMap<>();

        int servicesCount, hostsDownCount, hostsCount;

        // Check how many Hosts and Services are having trouble
            servicesCount = data.getJSONArray("services").length();
            hostsCount = data.getJSONArray("hosts").length();
            hostsDownCount = data.getJSONObject("status").getInt("num_hosts_down");
            hostListCount = hostsDownCount;

            String hostName, serviceName;
            String serviceDetails;
            int state;


            /*
            * Add all downed hosts to the list first in order to sort them to the top
             */
            for (int x = 0, y = 0; x < hostsCount; x++) {
                if (data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getInt("state") == 1) {
                    hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
                    hostPositions.put(hostName, hosts.size());
                    hosts.add(new Host(hostName, true));
                    y++;
                    if (y == hostsDownCount) {
                        break;
                    }
                }
            }

            for (int x=0 ; x < servicesCount ; x++) {
                hostName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("host_name");
                state = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("state");
                if (state != 0 && !hostPositions.containsKey(hostName)){
                    hostPositions.put(hostName, hosts.size());
                    hosts.add(new Host(hostName, false));
                    hostListCount++;
                }
            }

            /*
            * Loop through all services and store the hostname and the location of their respective failing services
             */
            for (int x=0 ; x < servicesCount ; x++) {
                hostName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("host_name");
                serviceName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("name");
                serviceDetails = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getJSONObject("last_check_result").getString("output");
                state = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("state");

                if (hostPositions.containsKey(hostName)) {
                    hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
                } else {
                    hostPositions.put(hostName, hosts.size());
                    hosts.add(new Host(hostName, false));
                    hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
                }
            }

        // Resort into alphabetical order with Downed and trouble hosts at the top
        Collections.sort(hosts);

        Bundle bundle = new Bundle();
        bundle.putSerializable("hosts", (Serializable) hosts);
        bundle.putInt("hostListCount", hostListCount);
        return bundle;
    }
}
