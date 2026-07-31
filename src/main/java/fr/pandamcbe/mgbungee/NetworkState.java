package fr.pandamcbe.mgbungee;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworkState {

    private Map<String, Integer> serversPlayerCount;
    private int TotalPlayers;
    private Set<String> serverNames;

    public NetworkState(){
        this.serverNames = new HashSet<>();
        this.serversPlayerCount = new HashMap<>();
        this.TotalPlayers = 0;
    }

    public Map<String, Integer> getServersPlayerCount() {
        return serversPlayerCount;
    }

    public void setServersPlayerCount(Map<String, Integer> serversPlayerCount) {
        this.serversPlayerCount = serversPlayerCount;
    }

    public int getTotalPlayers() {
        return TotalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        TotalPlayers = totalPlayers;
    }

    public Set<String> getServerNames() {
        return serverNames;
    }

    public void setServerNames(Set<String> serverNames) {
        this.serverNames = serverNames;
    }
}
