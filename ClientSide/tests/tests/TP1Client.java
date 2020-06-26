package tests;

import app.Client;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class TP1Client {

    /**
     * A)
     *  1. Serveur envoie un fichier de 40 Mo vers l'application client.
     *  2. Client envoie un fichier de 40 Mo vers l'application serveur.
     */
    @Test
    public void a1_downloadFile() throws IOException {
        //deletes file to receive for testing
        String s = System.getProperty("user.dir");
        s += "/receivedFiles/testFile.txt";

        File testFile = new File(s);
        if(testFile.exists()){
            System.out.println("test file is IN the receivedFiles folder");
            testFile.delete();
            System.out.println("test file has been deleted for testing purpose");
        } else {
            System.out.println("test file is NOT in the receivedFiles folder");
        }

        //ask for the test file
        InetAddress iPAddress = InetAddress.getByName("localhost");
        Client client = new Client(99, iPAddress);
        client.requestFile("testFile.txt");

        //assert receivedFiles in received
        Assert.assertTrue(testFile.exists());
    }

    //TODO : faire des tests pour prouver que les exigences du tp1 sont chill

    //B) protocole udp

    //C) communication malgre debranchement occasionel

    //D) Go-Back-N ou Selective Repeat

    //E) Taille de fenetre a l'emmeteur doit etre de 4 paquets ou plus

    //F) La taille des paquets doit etre ajuste pour etre inferieur a 65515 octets

    //G) Le serveur doit pouvoir accepter plusieurs clients en meme temps

    //H) Affichage qui permet de suivre la transmission
}