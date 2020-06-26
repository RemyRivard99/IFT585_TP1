package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        //concatoner header et buffer
        byte[] h = header.getBytes();
        byte[] combined = new byte[h.length + buf.length];

        for (int i = 0; i < combined.length; ++i)
        {
            combined[i] = i < h.length ? h[i] : buf[i - h.length];
        }

        return new DatagramPacket(combined, combined.length, ip, receiverPort);
    }

    public static synchronized DatagramPacket createAckPacket(InetAddress ip, int port, int packetNo, String fileName) {
        String ACKstring = "ACK";
        ACKstring += " ";
        ACKstring += packetNo;
        ACKstring += " ";
        ACKstring += fileName;

        byte buf[] = ACKstring.getBytes();

        return new DatagramPacket(buf, buf.length, ip, port);
    }
}