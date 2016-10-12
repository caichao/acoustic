package hust.cc.acoustic.util;

import java.util.TreeMap;

/**
 * Created by cc on 2016/10/12.
 */

public class AudioPlayerThread extends Thread{

    private AudioPlayer mAudioPlayer;
    private boolean isPlay = false;
    private boolean isClose = false;

    public AudioPlayerThread(){
        mAudioPlayer = new AudioPlayer();
    }

    public void play(){
        isPlay = true;
    }
    public void Pause(){
        isPlay = false;
    }

    public void close(){
        isPlay = false;
        mAudioPlayer.stop();
        mAudioPlayer.close();
    }

    @Override
    public void run() {
        super.run();

        while (true){
            while (isPlay){
                mAudioPlayer.play();
            }
            if(isClose) {
                break;
            }
        }
    }
}
