package hust.cc.acoustic.util;

/**
 * Created by cc on 2017/3/28.
 */

public class OFDM {

    private int Fc = 18000;  // up converting frequency
    private int F0 = 100;   // first subcarrier
    private int T = 1/F0;  // symbol length
    private int N = 20;  // number of subcarriers
    private int Fs = 48000; // sampling frequency
    private int B = 2000; // bandwidth

    public OFDM(){

    }


}
