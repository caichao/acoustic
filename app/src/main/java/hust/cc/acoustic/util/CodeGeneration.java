package hust.cc.acoustic.util;

/**
 * Created by cc on 2017/3/27.
 */

public class CodeGeneration {

    public static final int TypePN = 1;
    public static final int TypeChirp = 2;
    public static final int TypeZC = 3;
    public static final int TypeZCCode = 4;

    public static final int ZCLength = 4801 * 2;

    public int defaultType = TypePN;
    public int defautRepeat = 5;
    public int defautGap = 500;

    public short[] pcm = null;
    public short[] startWarm = null;
    public short[] endWarm = null;
    private int warmLength = 200;

    private int Fs = 48000;
    private int Maximum = 32768;
    private int F0 = 18000;

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
                pcm = new short[ZCLength];
                pcm = FileUtils.readTxt("zc.txt", ZCLength);break;
            default: break;
        }

        startWarm = new short[warmLength];
        endWarm = new short[warmLength];
        for(int i = 0; i < warmLength ; i++){
            startWarm[i] =  (short) ((Maximum / warmLength) * i * Math.cos(2 * Math.PI * i * F0 / Fs));
            endWarm[i] = (short)((Maximum / warmLength) * (warmLength - i) * Math.cos(2 * Math.PI * i * F0 / Fs));
        }
    }

}
