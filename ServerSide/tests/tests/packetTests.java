package tests;

import app.PacketFactory;
import app.PacketReader;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class packetTests {

    /**
     * On veut etre capable d'envoyer un numero de paquet specifique puisque le handler genere les packets
     * selon les ack recus, et de la fenetre.
     *
     * On verifie que les paquets cree ont le bon contenu.
     */
    @Test
    public void makeTransferPacket() throws IOException {
        InetAddress address = InetAddress.getLocalHost();    //irrelevent for test
        int port = 1010;
        int lastReceived = 3;
        int lastSent = 4;
        int windowSize = 2;
        String fileName = "testFile.txt";

        ArrayList<DatagramPacket> packetList = PacketFactory.makeDatagramPackets(address, port, lastReceived, lastSent, windowSize, fileName);

        for(int i = 0; i < packetList.size(); i++){
            PacketReader pr = new PacketReader(packetList.get(i));
            System.out.println("header : " + pr.getType());
            System.out.println("data : " + pr.getMessage());
        }

        //Pas d'assertions mais les donnees du paquet a verifier apparaissent dans la console.
    }

}
