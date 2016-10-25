package hust.cc.acoustic.communication;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2016/10/21.
 */

public class CommSocket extends Thread implements ICommService {

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private boolean isRunning = false;
    private String ip;
    private int port;

    private SocketFactory mSocketFactory;

    private volatile static CommSocket instance;
    private CommSocket(){};
    private BlockingQueue<short[]> queue ;
    private short[] pcm;

    private static final String TAG = CommSocket.class.getSimpleName();

    public static CommSocket getInstance(){
        if(instance == null){
            synchronized (CommSocket.class){
                if(instance == null){
                    instance = new CommSocket();
                }
            }
        }
        return instance;
    }

    public void init(String ip, int port){
        try {
            isRunning = true;
            queue = new LinkedBlockingQueue<short[]>(10);
            Log.d(TAG,"_________---------very important initialization");
            mSocketFactory = new SocketFactory(SocketFactory.TYPE_UDP,this.ip,this.port);
            /*mSocket = new Socket(ip,port);
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());*/
            Log.d(TAG,"------------ get the socket instance ------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setup(String ip, int port){
        this.ip = ip;
        this.port = port;
    }
    @Override
    public void run() {
        super.run();
        init(this.ip,this.port);
        while(isRunning){
            try {
                pcm = queue.take();
                mSocketFactory.send(pcm);
                /*if(mDataOutputStream != null ){
                    for(int i = 0;i<pcm.length;i++){
                        mDataOutputStream.writeShort(pcm[i]);
                    }
                }*/
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void send(short[] data) throws IOException, InterruptedException {
        short [] mData = new short[data.length];
        System.arraycopy(data,0,mData,0,data.length);
        queue.put(mData);
        /*if(mDataOutputStream != null){
            for(int i = 0 ; i < data.length ; i++){
                mDataOutputStream.writeShort(data[i]);
            }
        }*/
    }

    @Override
    public void close() {
        try {
            isRunning = false;
            mSocketFactory.close();
            /*if(mDataOutputStream != null)
                mDataOutputStream.close();
            if(mSocket != null)
                mSocket.close();*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
