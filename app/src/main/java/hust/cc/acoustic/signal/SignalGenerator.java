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
     * generate up chirp signal
     * @param fs : sampling frequency, units in Hz
     * @param B : bandwidth, units in Hz
     * @param T : period for the chirp signal, units in seconds
     * @param fmin : initial frequency, units in Hz
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
     *  generate down chirp signal
     * @param fs : sampling frequency, units in Hz
     * @param B : bandwidth, units in Hz
     * @param T : periodic duration of the chirp signal, units in Seconds
     * @param fmax : maxmimum frquency of the chirp signal, units in Hz
     * @return
     */
    public static short[] generateDownChirp(int fs, int B, float T, int fmax){
        int sampleLength = (int)(fs * T);
        short[] pcm = new short[sampleLength];
        double theta = 0;
        for(int i = 0; i < sampleLength ; i++){
            theta = 2 * pi * (fmax*i*1.0f/fs - B*i*i*1.0f/2/T/fs/fs);
            pcm[i] = (short)(32767 * Math.cos(theta));
        }
        return pcm;
    }

    /**
     *
     * @param fs : sampling frequency, uinit in Hz
     * @param finitial : the intial frequency of multiple tones, units in Hz
     * @param intervalFrequency : frequency gap between each tone, uinits in Hz
     * @param duration : duration for the whole signal, units in Seconds
     * @param numberOfToneIndice : the number of the pure tones, Integer value, should be larger than zero
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

    /**
     * generate fade in and fade out samples
     * @param fs : sample frquency in Hz
     * @param frquency : frquency of the pure tone signal, units in Hz
     * @param sampleLength : the sample length for the chirp or pure tone signal
     * @return
     */
    public static short[] generateFadeinFadeout(int fs, int frquency, int sampleLength){

        short[] pcmShort = new short[sampleLength];
        int ramp = sampleLength / 20;  // Amplitude ramp as a percent of sample count
        double theta = 0;
        for (int i = 0; i < ramp; ++i) {  // Ramp amplitude up (to avoid clicks)
            // Ramp up to maximum
            theta = 2 * pi * (frquency)*i*1.0f/fs;
            pcmShort[i] = (short) (32767 * Math.cos(theta) / ramp);
        }
        return pcmShort;
    }
}
