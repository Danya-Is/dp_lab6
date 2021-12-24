package ru.bmstu.ru.Messages;

import java.util.ArrayList;
import java.util.List;

public class ServersListMessage {

    private List<String> servers;

    public ServersListMessage(List<String> servers) {
        this.servers = servers;
    }

    public List<String> getServers() {
        return servers;
    }
}
