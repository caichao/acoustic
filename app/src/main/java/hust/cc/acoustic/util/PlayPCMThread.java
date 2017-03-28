package hust.cc.acoustic.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by cc on 2017/3/27.
 */

public class PlayPCMThread extends CodeGeneration implements Runnable {

    private AudioTrack mAudioTrack;
    private static final int F0 = 18000;
    private static final String TAG = AudioPlayer.class.getSimpleName();

    public PlayPCMThread(){}
    public PlayPCMThread(int type){
        super(type);
    }
    public PlayPCMThread(int type, int repeat, int gap){
        super(type, repeat, gap);
    }

    //@org.jetbrains.annotations.Contract(pure = true)
    private  long commonMultiple(long n, long m){
        return n*m/commonDivisor(n,m);
    }

    private long commonDivisor(long n,long m){
        long temp = 0;
        while (n%m != 0 ){
            temp = n%m;
            n = m;
            m = temp;
        }
        return m;
    }

    public void initAudioPlayer() {
        short buffer[];
        int rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        int minSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int size = (int)commonMultiple(rate,F0)/2;
        Log.d(TAG,"************ size = "+size);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size, AudioTrack.MODE_STREAM);


    }

    public void play(){
        if(mAudioTrack != null) {
            mAudioTrack.play();
            mAudioTrack.write(pcm,0, pcm.length);
        }
    }

    public void stop(){
        if(mAudioTrack != null)
        mAudioTrack.stop();
    }

    public void close(){
        if(mAudioTrack != null)
        mAudioTrack.release();
    }

    @Override
    public void run() {

        play();
        stop();
        close();
    }
}
