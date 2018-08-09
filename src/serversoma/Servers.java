/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.net.InetAddress;

/**
 *
 * @author renan
 */
public class Servers {
    String portServer;
    InetAddress ipServer;

    public Servers(String portServer, InetAddress ipServer) {
        this.portServer = portServer;
        this.ipServer = ipServer;
    }

    public String getPortServer() {
        return portServer;
    }

    public void setPortServer(String portServer) {
        this.portServer = portServer;
    }

    public InetAddress getIpServer() {
        return ipServer;
    }

    public void setIpServer(InetAddress ipServer) {
        this.ipServer = ipServer;
    }
    
}
