package app;

import java.net.InetAddress;

public class ClientInfo {
    InetAddress address;
    int port;
    int lastReceived;
    int lastSent;

    //file transfer info
    String fileName;
    int fileSize;       //fileSize is in packets

    ClientInfo(InetAddress ad, int po){
        address = ad;
        port = po;
        lastReceived = -1;
        lastSent = -1;
    }
}
