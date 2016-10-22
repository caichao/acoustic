package hust.cc.acoustic.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by cc on 2016/10/12.
 */

public class AudioPlayer {

    private AudioTrack mAudioTrack;
    private short[] mPCMData;
    private static final int F0 = 10000;
    private byte[] toneCode;

    public AudioPlayer(){
        initAudioPlayer();
    }

    public static long commonDivisor(long n,long m){
        long temp = 0;
        while (n%m != 0 ){
            temp = n%m;
            n = m;
            m = temp;
        }
        return m;
    }

    public static long commonMultiple(long n,long m){
        return n*m/commonDivisor(n,m);
    }

    public void initAudioPlayer(){
        short buffer[];
        int rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        int minSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
        Log.d(AudioPlayer.class.getSimpleName(),"************the rate is :"+rate);
        // Find a suitable buffer size
        Log.d(AudioPlayer.class.getSimpleName(),"************minSize = "+minSize);
        int size = (int)commonMultiple(rate,F0)/2;
        Log.d(AudioPlayer.class.getSimpleName(),"************ size = "+size);
        //size = rate / F0;
        /*int sizes[] = {1024, 2048, 4096, 8192, 16384, 32768};
        int size = 0;

        for (int s: sizes)
        {
            if (s > minSize)
            {
                size = s;
                break;
            }
        }*/

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size, AudioTrack.MODE_STREAM);

        mPCMData = new short[size];
        int j = 0;
        toneCode = new byte[size * 2];
        for(int i = 0; i < size ; i++){
            mPCMData[i] = (short)(32768 * Math.sin(2.0*Math.PI * (i) * F0/rate));
            toneCode[j++] = (byte)(mPCMData[i] & 0x00ff);
            toneCode[j++] = (byte)((mPCMData[i] & 0xff00)>>8);
        }


        mAudioTrack.play();
    }

    public void play(){
        mAudioTrack.play();
        mAudioTrack.write(mPCMData,0,mPCMData.length);
        //mAudioTrack.write(toneCode,0,toneCode.length);
    }

    public void stop(){
        mAudioTrack.stop();
    }

    public void close(){
        mAudioTrack.release();
    }
}
