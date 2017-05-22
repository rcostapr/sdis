package utils;

import java.net.Inet4Address;

public class Packet {

    private byte[] message;
    private String ip;

    public Packet(byte[] info, String ip){
        this.message=info;
        this.ip=ip;
    }

    public byte[] getMessage() {
        return message;
    }

    public String getIp() {
        return ip;
    }

}
