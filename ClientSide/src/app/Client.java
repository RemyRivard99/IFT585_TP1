package app;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {

    /* Global variables */
    //TODO : actually les rendre globales
    public static int BUFFER_SIZE = 256;
    private int THREAD_SLEEP = 0;
    private int PACKET_SIZE = 1024;

    /* Client info */
    private int port;
    InetAddress iPAddress;
    private DatagramSocket socket;
    int lastReceived;
    boolean running = true;

    public Client(int sendport, InetAddress address) throws SocketException {
        this.port = sendport;
        this.iPAddress = address;
        socket = new DatagramSocket();
    }

    public void requestFile(String fileName) throws IOException {
        System.out.println("requesting file " + fileName + " to port : " + port + ", on address : " + iPAddress);

        String s = "GETFILE";
        s += " ";
        s += fileName;
        s += " ";
        s += 0;
        byte[] data = s.getBytes();

        DatagramPacket request = new DatagramPacket(data, s.length(), iPAddress, port);
        socket.send(request);
        System.out.println("--Sending Getfile request as : " + s);

        byte[] answerData = new byte[512];
        System.out.println("--Receiving file by server");

        while (running) {
            try {
                socket.setSoTimeout(2000);
                System.out.println("--Receiving paquet");
                DatagramPacket answer = new DatagramPacket(answerData, answerData.length, iPAddress, port);
                socket.receive(answer);
                lastReceived++;

                System.out.println("--Sending ack");
                ClientResponder cr = new ClientResponder(answer, socket, this);
                cr.run();
            } catch (SocketTimeoutException e) {
                System.out.println("--Resending last ack");
                DatagramPacket ack = PacketFactory.createAckPacket(iPAddress, port, lastReceived, fileName);
                socket.send(ack);
            }
        }
    }

    public void setRunning(boolean b){
        running = b;
    }
}
