package hust.cc.acoustic.signal;

import hust.cc.acoustic.computation.DSP;

/**
 * Created by cc on 2017/9/5.
 */

public class  SignalGenerator {

    private int fs = 48000;
    private int B = 4000;
    private float T = 0.04f;
    private int fmin = 18000;

    private static double pi = Math.PI;

    public SignalGenerator(){

    }

    /*
    public SignalGenerator(int fs, int B, float T, int fmin){
        this.fs = fs;
        this.B = B;
        this.T = T;
        this.fmin = fmin;
    }*/

    /**
     *
     * @param fs : sampling frequency
     * @param B : bandwidth
     * @param T : period for the chirp signal
     * @param fmin : initial frequency
     * @return
     */
    public static short[] generateChirp(int fs, int B, float T, int fmin){
        int sampleLength = (int)(fs * T);
        short[] pcm = new short[sampleLength];
        double theta = 0;
        for(int i = 0; i < sampleLength ; i++){
            theta = 2 * pi * (fmin*i*1.0f/fs + B*i*i*1.0f/2/T/fs/fs);
            pcm[i] = (short)(32767 * Math.cos(theta));
        }
        return pcm;
    }

    /**
     *
     * @param fs : sampling frequency
     * @param finitial : the intial frequency of multiple tones
     * @param intervalFrequency : frequency gap between each tone
     * @param duration : duration for the whole signal
     * @param numberOfToneIndice : the number of the pure tones
     * @return pcm data
     */
    public static short[] generateMultiplePureTone(int fs, int finitial, int intervalFrequency, float duration, int numberOfToneIndice){
        int sampleLength = (int) (fs * duration);
        short[] pcm = new short[sampleLength];
        int[] pcmTmp = new int[sampleLength];
        double theta = 0;
        for (int i = 0; i < numberOfToneIndice ; i++){
            for(int j = 0; j < sampleLength ; j++){
                theta = 2 * pi * ((finitial + (i - 1) * intervalFrequency)*j*1.0f/fs);
                pcm[j] = (short)(32767 * Math.cos(theta));
                pcmTmp[j] += pcm[j];
            }
        }

        int maxValue = DSP.max(pcmTmp);
        Double ratio = maxValue / 32767.0;
        for(int i = 0 ; i < sampleLength ; i++){
            pcm[i] = (short) (Math.floor(pcmTmp[i]/ratio));
        }

        return pcm;
    }
}
