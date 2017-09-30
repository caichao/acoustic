package hust.cc.acoustic.computation;

/**
 * Created by cc on 2017/9/28.
 */

public class FirFilter {
    private int N;
    private double h[];
    private double y;
    private double x[];
    private int n;
    private int iWrite = 0;
    private int iRead = 0;

    public FirFilter(double h[]){
        this.h = h;
        this.N = h.length;
        this.x = new double[N];
    }

    public double filter(double newSample){
        y = 0;
        x[iWrite] = newSample;
        iRead = iWrite;
        for(n=0; n<N; n++){
            y += h[n] * x[iRead];
            iRead++;
            if(iRead == x.length){
                iRead = 0;
            }
        }
        iWrite--;
        if(iWrite < 0){
            iWrite = x.length-1;
        }
        return y;
    }

    public double[] filter(short[] input){
        double y[] = new double[input.length];
        for (int i = 0 ; i < input.length ; i++){
            y[i] = filter(input[i]);
        }
        return y;
    }
}
