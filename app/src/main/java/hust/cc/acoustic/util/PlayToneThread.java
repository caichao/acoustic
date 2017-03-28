package hust.cc.acoustic.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/**
 * Created by cc on 2016/10/17.
 */

public class PlayToneThread extends Thread{
    private boolean isPlaying = false;
    private final int freqOfTone;
    private AudioTrack audioTrack = null;
    //private final ToneStoppedListener toneStoppedListener;
    private float volume = 0f;

    private byte[] toneTable;
    private short[] toneSample;
    private int sampleLen = 2000;
    private int sampleRate = 48000;

    private static final String TAG = PlayToneThread.class.getSimpleName();
    /**
     * Instantiates a new Play tone thread.
     *
     * @param freqOfTone the freq of tone
     * @param volume the volume
     */
    public PlayToneThread(int freqOfTone, float volume) {
        this.freqOfTone = freqOfTone;
        //this.toneStoppedListener = toneStoppedListener;
        this.volume = volume;

    }

    @Override public void run() {
        super.run();
        playTone();

    }

    /**
     * This function is used to warm up the speaker
     */
    private void playTone() {

            int sampleRate = 48000;// 48 KHz

            double dnumSamples = (double) 0.1 * sampleRate;
            dnumSamples = Math.ceil(dnumSamples);
            int numSamples = (int) dnumSamples;
            double[] sample = new double[numSamples];
            byte[] generatedSnd = new byte[2 * numSamples];

            for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
                sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalized.
            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised.
            int idx = 0;
            int i;

            int ramp = numSamples / 20;  // Amplitude ramp as a percent of sample count

            for (i = 0; i < ramp; ++i) {  // Ramp amplitude up (to avoid clicks)
                // Ramp up to maximum
                final short val = (short) (sample[i] * 32767 * i / ramp);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
            toneTable = new byte[numSamples -ramp];
            int j = 0;
            for (i = ramp; i < numSamples; ++i) {                        // Max amplitude for most of the samples
                // scale to maximum amplitude
                final short val = (short) (sample[i] * 32767);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

            }

            /*for (i = numSamples - ramp; i < numSamples; ++i) { // do not Ramp amplitude down
                // Ramp down to zero
                final short val = (short) (sample[i] * 32767 * (numSamples - i));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }*/
            toneTable = new byte[numSamples*2];
            idx = 0;
            for (i = 0; i < numSamples; ++i) {                        // Max amplitude for most of the samples
                // scale to maximum amplitude
                final short val = (short) (sample[i] * 32767);
                // in 16 bit wav PCM, first byte is the low order byte
                toneTable[idx++] = (byte) (val & 0x00ff);
                toneTable[idx++] = (byte) ((val & 0xff00) >>> 8);

            }
        //isPlaying = true;

            try {
                int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                audioTrack =
                        new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

                // Sanity Check for max volume, set after write method to handle issue in android
                // v 4.0.3
                float maxVolume = AudioTrack.getMaxVolume();

                if (volume > maxVolume) {
                    volume = maxVolume;
                } else if (volume < 0) {
                    volume = 0;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioTrack.setVolume(volume);
                } else {
                    audioTrack.setStereoVolume(volume, volume);
                }
                Log.i(TAG,"before another try ------------ get audio state:"+audioTrack.getState());

                audioTrack.play(); // Play the track

                int ret = audioTrack.write(generatedSnd, 0, generatedSnd.length);    // Load the track

                Log.i(TAG,"#######################start the play loop####################");
                Log.i(TAG,"ret result = "+ret);
                i = 0;
                /*while (isPlaying){

                    //audioTrack.play();
                    audioTrack.write(generatedSnd,ramp,generatedSnd.length);
                    Log.d(TAG,"i = " +(i++));
                }*/
                //audioTrack.play();

                while (isPlaying) {
                    ret = audioTrack.write(toneTable, 0, toneTable.length);
                }

                switch (ret){
                    case AudioTrack.ERROR_INVALID_OPERATION:
                        Log.i(TAG,"invalid operation"); break;
                    case AudioTrack.ERROR:
                        Log.i(TAG,"unknown error");break;
                    case AudioTrack.ERROR_BAD_VALUE:
                        Log.i(TAG,"bad value");break;
                    case AudioTrack.ERROR_DEAD_OBJECT:
                        Log.i(TAG,"need recreate an audioTrack");break;
                    default:
                        Log.i(TAG,"a successful wirte");break;
                }

                Log.i(TAG,"after another try --------------get audio state:"+audioTrack.getState());
                Log.i(TAG,"ret result = "+ret);

                Log.i(TAG,"$$$$$$$$$$$$$$$$$$$$$$end the play loop $$$$$$$$$$$$$$$$$$$$$$");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //stopTone();
    }

    public void enablePlay(){
        if(!isPlaying){
            isPlaying = true;
        }
    }
    /**
     * Stop tone.
     */
    public void stopTone() {
        isPlaying = false;
        if (audioTrack != null && audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
        }
    }
}
