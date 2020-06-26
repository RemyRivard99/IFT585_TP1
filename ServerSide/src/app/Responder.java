package app;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Responder extends Thread {

    DatagramSocket socket = null;
    DatagramPacket packet;
    int outputPort;
    InetAddress inet;
    boolean resend;

    public Responder(DatagramPacket packet, DatagramSocket socket, int port, InetAddress inet) {
        this.packet = packet;
        System.out.println(new String(packet.getData()));
        this.socket = socket;
        outputPort = port;
        this.inet = inet;
        this.resend = false;
    }

    public Responder(int port, InetAddress inet) {
        this.packet = null;
        outputPort = port;
        this.inet = inet;
        this.resend = true;
    }

    @Override
    public void run() {
        try {
            ArrayList<DatagramPacket> dataList = new ArrayList<>();

            if(resend) {
                ClientInfo client = Server.findClient(inet, outputPort);
                dataList = PacketFactory.makeDatagramPackets(inet, outputPort, client.lastReceived, client.lastSent, Server.MAX_WINDOWS, client.fileName);
            } else {
                dataList = makeResponse(packet);
            }

            //pour chaque paquet produit par GoBackN
            for(int i = 0; i < dataList.size() ; i++){
                socket.send(dataList.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Décodage et appel d'une transaction */
    private ArrayList<DatagramPacket> executeTransaction(StringTokenizer tokenizer) {
        try {
            String command = tokenizer.nextToken();

            /** Un client demande un fichier */
            if (command.equals("GETFILE")) {
                String fileName = (String) read(tokenizer, "string");
                int packetNo = (int) read(tokenizer, "int");

                //ajoute le client a la liste de clients
                ClientInfo newClient = new ClientInfo(inet, outputPort);
                Server.clients.add( newClient );

                //Determine la taille du fichier pour savoir quand arreter le transfert
                String path = System.getProperty("user.dir");
                path += "/fileToSend/";
                path += fileName;
                File file = new File(path);

                newClient.fileSize = (int)file.length()/Server.BUFFER_SIZE;

                ArrayList<DatagramPacket> dataList = new ArrayList<>();
                dataList.add( PacketFactory.createTransferPacket(inet, outputPort, packetNo, fileName, Server.BUFFER_SIZE));
                return dataList;

            /** Un client acquiese avoir recu un fichier */
            } else if (command.equals("ACK")){
                int paquetNo = (int) read(tokenizer, "int");

                ClientInfo client = Server.findClient(inet, outputPort);

                //si c'est le dernier ACK, arrete l'algorithme
                if(paquetNo == client.fileSize) return null;

                //appliquer la logique pour bouger lastSent
                if(paquetNo == client.lastReceived) client.lastReceived++;
                if(client.lastSent - client.lastReceived < Server.MAX_WINDOWS) client.lastSent++;

                return PacketFactory.makeDatagramPackets(inet, outputPort, client.lastReceived, client.lastSent, Server.MAX_WINDOWS, client.fileName);
            }
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
                    token = token.trim();
                    try {
                        value = Integer.parseInt(token);
                    } catch (NumberFormatException e) {
                        throw new Exception("Nombre attendu à la place de \"" + token + "\"");
                    }
                    break;
            }
        } else throw new Exception ("Nombre de paramètres insuffisant");
        return value;
    }

    /** Converti un paquet en String. */
    public String readPaquet(DatagramPacket packet) {
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
}
