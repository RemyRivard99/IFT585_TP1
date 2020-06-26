package app;

import java.net.DatagramPacket;

public class PacketReader {
    String type;
    String msg = null;
    int port;

    public PacketReader(DatagramPacket packet) {
        String data = new String(packet.getData(), 0, packet.getLength());

        //dataList[0] = type
        String[] dataList = data.split(";;;");
        if(dataList.length > 1) {
            this.msg = dataList[1];
        }

        //dataList[1] = data
        dataList = dataList[0].split("&");
        this.type = dataList[0];

        this.port =  packet.getPort();
    }

    public PacketReader(String data){
        //dataList[0] = type
        String[] dataList = data.split(";;;");
        if(dataList.length > 1) {
            this.msg = dataList[1];
        }

        //dataList[1] = data
        dataList = dataList[0].split("&");
        this.type = dataList[0];
    }

    public String getMessage(){return msg;}
    public String getType() {return type;}
    public int getPort(){return port;}
}
