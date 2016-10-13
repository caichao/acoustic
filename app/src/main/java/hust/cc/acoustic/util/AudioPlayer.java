package hust.cc.acoustic.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by cc on 2016/10/12.
 */

public class AudioPlayer {

    private AudioTrack mAudioTrack;
    private short[] mPCMData;
    private static final int F0 = 18000;

    public AudioPlayer(){
        initAudioPlayer();
    }

    public void initAudioPlayer(){
        short buffer[];
        int rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        int minSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

        // Find a suitable buffer size
        int sizes[] = {1024, 2048, 4096, 8192, 16384, 32768};
        int size = 0;

        for (int s: sizes)
        {
            if (s > minSize)
            {
                size = s;
                break;
            }
        }

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                size, AudioTrack.MODE_STREAM);

        mPCMData = new short[size];
        for(int i = 0; i < size ; i++){
            mPCMData[i] = (short)(32768 * Math.sin(2.0*Math.PI * (i) * F0/rate));
        }

        mAudioTrack.play();
    }

    public void play(){
        mAudioTrack.write(mPCMData,0,mPCMData.length);
    }

    public void stop(){
        mAudioTrack.stop();
    }

    public void close(){
        mAudioTrack.release();
    }
}
