package hust.cc.acoustic.computation;

/**
 * Created by cc on 2017/9/28.
 */

public class MathCC {

    private int i = 0;
    /**
     * get the maximum value for a double array
     * @param x: the input double array
     * @return: the maximum value of the array
     */
    public static double getMax(double [] x){
        double maxVal = Double.MIN_VALUE;
        for(int i = 0; i < x.length; i++){
            if(x[i] > maxVal){
                maxVal = x[i];
            }
        }
        return maxVal;
    }

    /**
     * get the location of the maximum value in the double array
     * @param x : the input double array
     * @return the index of the maximum value
     */
    public static int getMaxIndex(double[] x){
        int idx = 0;
        double maxVal = Double.MIN_VALUE;
        for(int i = 1; i < x.length; i++){
            if(x[i] > maxVal){
                maxVal = x[i];
                idx = i;
            }
        }
        return idx;
    }
}
