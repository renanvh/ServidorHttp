/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversoma;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author renan
 */
public class RespUnicast extends Thread {
    List<Servers> servers;

    public RespUnicast(List<Servers> server) throws IOException {
        this.servers = server;
        this.start();
    }

    public List<Servers> getServers() {
        return servers;
    }

    public void setServers(List<Servers> servers) {
        this.servers = servers;
    }

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(6666);
            while (true) {
                Socket s = listenSocket.accept();
                ClienteDaResp clientRespUnicast = new ClienteDaResp(s, this.servers);
            }
        } catch (IOException ex) {
            Logger.getLogger(RespUnicast.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    class ClienteDaResp extends Thread {

        Socket s;
        DataInputStream in;
        List<Servers> servers;

        public ClienteDaResp(Socket s, List<Servers> server) throws IOException {
            this.s = s;
            this.in = new DataInputStream(s.getInputStream());
            this.servers = server;
            this.start();
        }

        public Socket getS() {
            return s;
        }

        public void setS(Socket s) {
            this.s = s;
        }

        public DataInputStream getIn() {
            return in;
        }

        public void setIn(DataInputStream in) {
            this.in = in;
        }

        public List<Servers> getServers() {
            return servers;
        }

        public void setServers(List<Servers> servers) {
            this.servers = servers;
        }

        @Override
        public void run() {
            String msg = new String();
            while(true){
                try {
                    msg=in.readUTF();
                    if(msg.contains("AD")){
                        String[] splitter;
                        splitter = msg.split("AD");
                        Servers novoServer =new Servers(splitter[1], this.s.getInetAddress());
                        servers.add(novoServer);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RespUnicast.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }        
    }
}
