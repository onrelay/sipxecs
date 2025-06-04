package org.sipfoundry.commons.jetty;

import org.eclipse.jetty.server.ServerConnector;

import org.eclipse.jetty.server.Server;

public class SocketFactory {
    public static ServerConnector createSocketListener(int port) {
        Server server = new Server(); // Create an internal server instance
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setIdleTimeout(60000);         
        connector.setAcceptQueueSize(50); 
        connector.setReuseAddress(true); 
        return connector;
    }
}
