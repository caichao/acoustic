package hust.cc.acoustic.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by cc on 2016/10/25.
 */

public class SocketFactory implements ICommService{

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private DatagramSocket mDatagramSocket;

    public static final int TYPE_TCP = 1;
    public static final int TYPE_UDP = 2;

    private String ip;
    private int port;
    private int type;
    private InetAddress udpIP;

    public SocketFactory(int type,String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.type = type;
        if(type == TYPE_TCP){
            mSocket = new Socket(ip,port);
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
        }else if(type == TYPE_UDP){
            mDatagramSocket = new DatagramSocket(port);
            udpIP = InetAddress.getByName(ip);
        }
    }

    @Override
    public void send(short[] data) throws IOException, InterruptedException {

        switch (type){
            case TYPE_TCP:
                /*short [] tmp = new short[data.length];
                System.arraycopy(data,0,tmp,0,tmp.length);*/
                for(int i = 0 ; i < data.length ; i++){
                    mDataOutputStream.writeShort(data[i]);
                }
                break;
            case TYPE_UDP:
                byte[] datatmp = new byte[data.length * 2];
                for(int i = 0 ; i < data.length ; i++){
                    datatmp[i*2] = (byte) ((data[i]>>8) & 0x00ff);
                    datatmp[i*2+1] = (byte) (data[i] & 0x00ff);
                }
                DatagramPacket datagramPacket = new DatagramPacket(datatmp,datatmp.length,udpIP,port);
                mDatagramSocket.send(datagramPacket);
                break;
        }
    }

    @Override
    public void close() throws IOException {
        if(mDatagramSocket != null)
            mDatagramSocket.close();
        if(mSocket != null)
            mSocket.close();
    }
}
