package app;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ClientResponder extends Thread {

    DatagramSocket socket;
    DatagramPacket packet;

    public ClientResponder(DatagramPacket packet, DatagramSocket socket) {
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            ArrayList<DatagramPacket> dataList = makeResponse(packet);

            //initialise le socket
            socket = new DatagramSocket();

            //pour chaque paquet
            for(int i = 0; i < dataList.size() ; i++){
                //socket.send(dataList.get(i));
                System.out.println(new String(dataList.get(i).getData()));
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<DatagramPacket> makeResponse(DatagramPacket packet) throws Exception {
        return handleRequest(readPaquet(packet));
    }

    /** Traitement des requetes */
    public ArrayList<DatagramPacket> handleRequest(String request) throws Exception
    {
        Reader inputString = new StringReader(request);
        BufferedReader reader = new BufferedReader(inputString);
        String transaction = readTransaction(reader);
        while (!transactionDone(transaction))
        {
            StringTokenizer tokenizer = new StringTokenizer(transaction, " ");
            if (tokenizer.hasMoreTokens())
                return executeTransaction(tokenizer);
            transaction = readTransaction(reader);
        }
        return null;
    }

    /** Décodage et appel d'une transaction */
    private ArrayList<DatagramPacket> executeTransaction(StringTokenizer tokenizer) {
        try {
            String command = tokenizer.nextToken();

            /** Envoie un ACK pour un transfert */
            if (command.equals("TRNSFR")) {
                //Lis les donnees du paquet
                int packetNo = (int) read(tokenizer, "int");
                String fileData = (String) read(tokenizer, "string");

                while(tokenizer.hasMoreTokens()){
                    fileData += (String) read(tokenizer, "string");
                }

                PacketReader pr = new PacketReader(fileData);
                System.out.println("---received packet no: " + packetNo + " for file: " + pr.type + " with data: " + pr.getMessage());

                String data = readPaquet(packet);
                PacketReader pr2 = new PacketReader(data);
                System.out.println("---with data: ");
                System.out.println(pr2.getMessage());

                //Ajoute les donnees au fichier
                String path = System.getProperty("user.dir");
                path += "/receivedFiles/";
                path += pr.type;
                File file = new File(path);

                //Ecrit dans le fichier
                FileOutputStream fos = new FileOutputStream(file.getPath());
                fos.write(pr2.getMessage().getBytes());

                //Envoie un ACK pour confirmer le paquet
                ArrayList<DatagramPacket> dataList = new ArrayList<>();
                dataList.add( PacketFactory.createAckPacket(packet.getAddress(), packet.getPort(), packetNo));
                return dataList;

                /** recoit le dernier paquet et arrete */
            } else if (command.startsWith("LAST")){
                /*
                int paquetNo = (int) read(tokenizer, "int");

                ClientInfo client = Server.findClient(packet.getAddress(), packet.getPort());

                //si c'est le dernier ACK, arrete l'algorithme
                if(paquetNo == client.fileSize) return null;

                //appliquer la logique pour bouger lastSent
                if(paquetNo == client.lastReceived) client.lastReceived++;
                if(client.lastSent - client.lastReceived < Server.MAX_WINDOWS) client.lastSent++;

                return PacketFactory.makeDatagramPackets(packet.getAddress(), packet.getPort(), client.lastReceived, client.lastSent, Server.MAX_WINDOWS, client.fileName);
                 */
            }
            /*
            else if (command.equals("RECEIVEFILE")) {
                System.out.println("helloWorld!!!");
                String fileName = (String) read(tokenizer, "string");
                receiveFile(fileName, client.port, client.address);
                return receivedAck();

            }
            */
            else System.out.println("  Requete invalide.  Essayer \"help\"");
        }
        catch (Exception e) {
            System.out.println("** " + e.toString());
        }

        //TODO : faut arranger ca
        return null;
    }

    //---------------------------

    /** Converti les chaînes de caractères en objet. */
    private Object read(StringTokenizer tokenizer, String type) throws Exception {
        Object value = null;
        if(tokenizer.hasMoreElements()){
            switch (type){
                case "string" :
                    value = tokenizer.nextToken();
                    break;

                case "int" :
                    String token = tokenizer.nextToken();
                    try {
                        value = Integer.valueOf(token);
                    } catch (NumberFormatException e) {
                        throw new Exception("Nombre attendu à la place de \"" + token + "\"");
                    }
                    break;
            }
        } else throw new Exception ("Nombre de paramètres insuffisant");
        return value;
    }

    /** Converti un paquet en String. */
    public String readPaquet(DatagramPacket packet) throws IOException {
        return new String(packet.getData());
    }

    /** Lecture d'une transaction */
    private String readTransaction(BufferedReader reader) throws IOException {
        System.out.print("> ");
        String transaction = reader.readLine();
        return transaction;
    }

    /** Vérifie s'il reste des transactions à executer. */
    private boolean transactionDone(String transaction){
        if (transaction == null) return true;
        StringTokenizer tokenizer = new StringTokenizer(transaction, " ");

        if (!tokenizer.hasMoreTokens()) return false;
        String commande = tokenizer.nextToken();
        return commande.equals("exit");
    }
}
