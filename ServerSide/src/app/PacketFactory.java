package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class PacketFactory {

    /**
     *  Quand on veut un paquet de n'importe quel type, on appel la factory pour nous le faire.
     */
    public static synchronized DatagramPacket createTransferPacket(InetAddress ip, int receiverPort, int packetNo, String fileName, int bufferSize) throws IOException {
        //Header
        String header = "TRNSFR";
        header += " ";
        header += packetNo;
        header += " ";
        header += fileName;
        header += "&";

        //Separator for PacketReader
        header += ";;;";

        //Content
        String path = System.getProperty("user.dir");
        path += "/fileToSend/";
        path += fileName;
        byte[] array = Files.readAllBytes(Paths.get(path));

        //faire un paquet pour packetNo de longueur BufferSize
        int startIndex = bufferSize * packetNo;
        int endIndex = startIndex + bufferSize;
        byte[] buf = Arrays.copyOfRange(array, startIndex, endIndex);

        // convert bytes[] to string
        System.out.println("Sending : " + new String(buf) + " of length : " + buf.length);

        //concatoner header et buffer
        byte[] h = header.getBytes();
        byte[] combined = new byte[h.length + buf.length];

        for (int i = 0; i < combined.length; ++i)
        {
            combined[i] = i < h.length ? h[i] : buf[i - h.length];
        }

        return new DatagramPacket(combined, combined.length, ip, receiverPort);
    }

    public static ArrayList<DatagramPacket> makeDatagramPackets(InetAddress address, int port, int lastReceived, int windowSize, String fileName) throws IOException {
        ArrayList<DatagramPacket> dataList = new ArrayList<DatagramPacket>();

        for(int e = 0; e < windowSize; e++){
            int packetNo = e + lastReceived;
            if(packetNo > Server.lastPacket(fileName)){
                return dataList;
            }

            if(packetNo == Server.lastPacket(fileName)){
                DatagramPacket packet = makeLastPacket(address, port, packetNo, Server.BUFFER_SIZE, fileName);
                dataList.add(packet);
            } else {
                DatagramPacket packet = createTransferPacket(address, port, packetNo, fileName, Server.BUFFER_SIZE);
                dataList.add(packet);
            }
        }
        return dataList;
    }

    public static DatagramPacket makeLastPacket(InetAddress inet, int outputPort, int paquetNo, int maxWindows, String fileName) throws IOException {
        //Header
        String header = "LAST";
        header += " ";
        header += paquetNo;
        header += " ";
        header += fileName;
        header += "&";

        //Separator for PacketReader
        header += ";;;";

        //Content
        String path = System.getProperty("user.dir");
        path += "/fileToSend/";
        path += fileName;
        byte[] array = Files.readAllBytes(Paths.get(path));

        //faire un paquet pour packetNo de longueur BufferSize
        int startIndex = Server.BUFFER_SIZE * (paquetNo);
        int endIndex = array.length;
        byte[] buf = Arrays.copyOfRange(array, startIndex, endIndex);

        // convert bytes[] to string
        System.out.println("Sending : " + new String(buf) + " of length : " + buf.length);

        //concatoner header et buffer
        byte[] h = header.getBytes();
        byte[] combined = new byte[h.length + buf.length];

        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < h.length ? h[i] : buf[i - h.length];
        }

        return new DatagramPacket(combined, combined.length, inet, outputPort);
    }
}
