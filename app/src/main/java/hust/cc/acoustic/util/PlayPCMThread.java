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
    private int rate = 0;
    private int size = 0;

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
        rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        int minSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        size = (int)commonMultiple(rate,F0)/2;
        Log.d(TAG,"************ size = "+size);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size, AudioTrack.MODE_STREAM);
    }

    public void play(){
        if(mAudioTrack != null) {
            mAudioTrack.play();
            //mAudioTrack.write(pcm,0, pcm.length);
            // start to warm up the speaker
            byte[] startPCM = new byte[startWarm.length * 2];
            int index = 0;
            for(int i = 0 ; i < startWarm.length ; i++){
                startPCM[index++] =  (byte) (startWarm[i] & 0x00ff);
                startPCM[index++] = (byte) ((startWarm[i] & 0xff00) >> 8 ) ;
            }

            //start to play the sequence data
            index = 0;
            byte[] pcmSequence = new byte[Sequence.length * 2];
            for(int i = 0 ; i < Sequence.length * 2 ; i++){
                pcmSequence[index++] = (byte) (Sequence[i] & 0x00ff);
                pcmSequence[index++] = (byte) ((Sequence[i] & 0xff00) >> 8 );
            }

            index = 0;
            byte[] endPCM = new byte[endWarm.length];
            for(int i = 0; i < endWarm.length; i++){
                endPCM[index++] = (byte) (endWarm[i] & 0x00ff);
                endPCM[index++] = (byte) ((endWarm[i] & 0xff00) >> 8 );
            }

            byte[] v = new byte[defautGap * 2];
            for(int i = 0 ; i < v.length ; i++){
                v[i] = 0;
            }

            mAudioTrack.write(startPCM, 0, startPCM.length);

            int N = pcmSequence.length / size;

            for(int j = 0; j < defautRepeat; j++) {
                for (int i = 0; i < N; i++)
                    mAudioTrack.write(pcmSequence, i * size, size);
                mAudioTrack.write(pcmSequence, N * size, pcmSequence.length - N * size);

                // filing the gap
                mAudioTrack.write(v,0,v.length);
            }

            mAudioTrack.write(endPCM, 0, endPCM.length);
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
