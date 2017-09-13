package se.standersson.icingalert;

import java.util.List;

/*
 * Holds the parsed information about hosts from Icinga
 */

class HostSingleton {
    private static HostSingleton hostSingleton;
    private List<Host> hosts;


    static synchronized HostSingleton getInstance() {
        if (hostSingleton == null) {
            hostSingleton = new HostSingleton();
        }
        return hostSingleton;
    }

    void putHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    List<Host> getHosts() {
        return hosts;
    }

    int findHostName(String hostname) {
        for ( int x=0 ; x<hosts.size() ; x++) {
            if (hosts.get(x).getHostName().equals(hostname)) {
                return x;
            }
        }
        return -1;
    }

    int findServiceName(int host, String serviceName) {
        return hosts.get(host).findServiceName(serviceName);
    }
}
