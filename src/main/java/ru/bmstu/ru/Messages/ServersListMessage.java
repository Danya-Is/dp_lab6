package ru.bmstu.ru.Messages;

import java.util.ArrayList;

public class ServersListMessage {

    private ArrayList<String> servers;

    public ServersListMessage(ArrayList<String> servers) {
        this.servers = servers;
    }

    public ArrayList<String> getServers() {
        return servers;
    }
}
