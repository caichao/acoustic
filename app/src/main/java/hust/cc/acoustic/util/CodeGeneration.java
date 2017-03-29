package hust.cc.acoustic.util;

/**
 * Created by cc on 2017/3/27.
 */

public class CodeGeneration {

    public static final int TypePN = 1;
    public static final int TypeChirp = 2;
    public static final int TypeZC = 3;
    public static final int TypeZCCode = 4;

    public static final int ZCLength = 481 * 2;
    public static final int ChirpLength = 480;
    public static final int k = 200000;


    public int defaultType = TypePN;
    public int defautRepeat = 5;
    public int defautGap = 500;


    public short[] Sequence = null;  // the sequence that can be directly transmitted
    public short[] startWarm = null; // the start sequence to warm up the speaker
    public short[] endWarm = null; // the end sequence to slowly close the speaker
    private int warmLength = 200; //

    private int Fs = 48000;
    private int Maximum = 32768;
    private int F0 = 18000;
    private int N = 20;

    public CodeGeneration(){
        initParams();
    }
    public CodeGeneration(int type){
        defaultType = type;
        initParams();
    }
    public CodeGeneration(int type, int repeat, int gap){
        defaultType = type;
        defautRepeat = repeat;
        defautGap = gap;
        initParams();
    }

    private void initParams(){
        switch (defaultType) {
            case TypeZC:
                OFDMSequenceGeneration();
                break;
            case TypeChirp:
                Sequence = new short[ChirpLength];
                for (int i = 0; i < ChirpLength ; i++){
                    Sequence[i] = (short) (Maximum * Math.cos(Math.PI * 2 * (F0 + k * i) * i / Fs));
                }
                break;
            default: break;
        }

        startWarm = new short[warmLength];
        endWarm = new short[warmLength];
        for(int i = 0; i < warmLength ; i++){
            startWarm[i] =  (short) ((Maximum / warmLength) * i * Math.cos(2 * Math.PI * i * F0 / Fs));
            endWarm[i] = (short)((Maximum / warmLength) * (warmLength - i) * Math.cos(2 * Math.PI * i * F0 / Fs));
        }
    }

    private void OFDMSequenceGeneration(){

        Sequence = new short[ZCLength/2];

        OFDM ofdm = new OFDM();
        double[] tmp = new double[ZCLength];
        tmp = FileUtils.readTxt("zc.txt", ZCLength);
        int segment = (int)Math.floor(ZCLength / 2.0f / N);
        //float tmp = new float[]
        double[] symbol = new double[N + N/4];
        double[] zc = new double[2*N];

        for(int i = 0 ; i < segment ; i++){
            System.arraycopy(tmp,i*N, zc, 0, N);
            System.arraycopy(tmp,i*N + ZCLength/2, zc, 0, N);
            ofdm.modulation(zc);
            ofdm.addCP();
            symbol = ofdm.getSymbol();

            System.arraycopy(symbol, 0, Sequence, i * symbol.length, symbol.length);
        }

        //the following code are used to tackle the last segment which may not have a symbol length
        for(int i = segment * N; i < ZCLength / 2; i++){
            zc[i] = tmp[i];
            zc[i+N] = tmp[i + ZCLength/2];
        }
        for(int i = N - (ZCLength/2 - segment * N) ; i < N ; i++){
            zc[i] = Maximum;
            zc[i + N] = Maximum;
        }

    }

}
