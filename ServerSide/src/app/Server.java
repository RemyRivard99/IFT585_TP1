package app;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Server implements Runnable {

    /* Server info */
    private DatagramSocket socket;
    private int port;

    public final static int TIMEOUT = 0;  //doit etre 5000
    public final static int MAX_WINDOWS = 5;
    public final static int BUFFER_SIZE = 256;

    public static ArrayList<ClientInfo> clients;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<ClientInfo>();
    }

    @Override
    public void run() {
        //TODO : Peut etre que la taille du paquet envoyer doit etre exactement le meme que celui ci pour etre acceptee
        //soit: header + msg
        byte[] packetBuffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(packetBuffer, BUFFER_SIZE);
        while(true){
            try {
                socket = new DatagramSocket(port);
                socket.setReceiveBufferSize(BUFFER_SIZE);
                socket.setSendBufferSize(BUFFER_SIZE);
                socket.setSoTimeout(TIMEOUT);

                System.out.println("WAITING FOR CONNECTION PACKET ON PORT : " + port + " / ON ADDRESS : " + socket.getInetAddress());
                socket.receive(packet);

                Responder r = new Responder(packet, socket, packet.getPort(), packet.getAddress());
                r.run();
                Arrays.fill(packetBuffer, (byte)0);     //enleve le contenu du buffer
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        }
    }

    public void resendPackets(){
        System.out.println("Timeout exception happened, resending paquets");
        for(ClientInfo client : clients) {
            Responder r = new Responder(client.port, client.address);
            r.run();
        }
    }

    public static ClientInfo findClient(InetAddress inet, int port) {
        for(ClientInfo client : clients) {
            if(client.address == inet && client.port == port) {
                return client;
            }
        }
        return null;
    }

    public static void removeClient(InetAddress address, int port) {
        for(ClientInfo client : clients) {
            if(client.address == address && client.port == port) {
                Server.clients.remove(client);
            }
        }
    }
}

