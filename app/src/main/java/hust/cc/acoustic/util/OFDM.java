package hust.cc.acoustic.util;

/**
 * Created by cc on 2017/3/28.
 */

public class OFDM {

    private int Fc = 18000;  // up converting frequency
    private int F0 = 100;   // first subcarrier
    private float T = 1.0f/F0;  // symbol length
    private int N = 20;  // number of subcarriers
    private int Fs = 48000; // sampling frequency
    private int B = 2000; // bandwidth
    private int L = (int)(Fs * T);
    private int CPL = (int)Math.floor(N/4);

    private double[] signal = null;
    private double[] symbol = null;
    private double[] Q = null;
    private double[] I = null;
    private int[] F = null;

    public OFDM(){
        symbol = new double[L + CPL];
        signal = new double[L];
        Q = new double[L];
        I = new double[L];
        F = new int[N];
        for (int i = 1 ; i <= N ; i++){
            F[i-1] = Fc + F0 * i;
        }
    }

    /**
     * modulate the signal at different subcarriers
     * the length of the signal is L * 2, the first half is Q part while the later is I part
     * @param timeSignal
     */
    public void modulation(double[] timeSignal){
        double[] tmp = new double[L];
        for(int i = 0; i < N ; i++){  // modulate the signal at time domain
            for(int j = 0; j < L ; j++){
                Q[j] = timeSignal[i] * Math.cos(2 * Math.PI * F[i] * j / Fs);
                I[j] = timeSignal[i] * Math.sin(2 * Math.PI * F[i] * j / Fs);
                signal[j] = Q[j] + I[j];
            }
        }
    }

    public void addCP(){
        System.arraycopy(signal,0,symbol,0,L);
        System.arraycopy(signal,0,symbol,L,CPL);
    }

    /**
     * This function is used to suppress the volum which is too large
     */
    public void normalization(){

    }

    public double[] getSymbol(){
        return symbol;
    }
}
