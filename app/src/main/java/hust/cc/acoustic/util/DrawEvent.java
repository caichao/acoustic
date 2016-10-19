package hust.cc.acoustic.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import hust.cc.acoustic.computation.Complex;
import hust.cc.acoustic.computation.FFT;

/**
 * Created by cc on 2016/10/13.
 */

public class DrawEvent implements SurfaceHolder.Callback, AudioRecorder.RecordingCallback {

    private int Width = 0;
    private int Height = 0;
    private DrawingThread mThread;
    private boolean enableLog = false;

    private static final String TAG = DrawEvent.class.getSimpleName();
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new DrawingThread(surfaceHolder);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Width = i1;
        Height = i2;
        mThread.updateWindow(Width,Height);
        Log.d(DrawEvent.class.getSimpleName(),"-------------------Width="+Width);
        Log.d(DrawEvent.class.getSimpleName(),"-------------------Height="+Height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mThread.quit();
    }

    @Override
    public void onDataReady(short[] data,int bytelen) {
        //Log.d(DrawEvent.class.getSimpleName(),"-------------byte length : "+data.length);
        if(mThread != null) {
            mThread.notifyDataChange(data);
            //Log.d(TAG,"valid data length = "+bytelen); //output 2048
        }
    }

    public void setEnableLog(){
        if(!enableLog){
            enableLog = false;
            if(mThread != null)
                mThread.enableLog();
        }
    }

    private class DrawingThread extends Thread{
        private SurfaceHolder mSurfaceHolder;
        private int mDrawingWidth,mDrawingHeight;
        private Paint mPaint;
        private Paint textPaint;
        private short[] data;
        private Canvas mCanvas;
        private boolean isDrawing = true;
        private boolean isRefresh = false;
        private boolean isLog = false;

        private int x;
        private int y;
        private int shift;
        private int peak;
        private float frequency;
        private float deltF = 10.76666f;
        //FFT about
        private static final int DataSize = 2048;
        short[] pcmData ;
        float[] fftResult;
        float[] fftHalf;
        FFT fft ;
        Complex[] complexData;

        //moving average window;
        int searchRange = 10;
        int windowLength = 50;
        private float[] filteredBand ;
        private MovingAverage movingAverage;

        public DrawingThread(SurfaceHolder mSurfaceHolder){
            this.mSurfaceHolder = mSurfaceHolder;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(2.0f);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setStrokeWidth(2.0f);

            complexData = new Complex[DataSize];
            for(int i = 0;i<complexData.length;i++){
                complexData[i] = new Complex();
            }
            pcmData = new short[DataSize];
            fftResult = new float[DataSize];
            fft = new FFT(DataSize);
            fftHalf = new float[DataSize/2];//only half result is valid
            deltF = 48000 / DataSize;
            movingAverage = new MovingAverage(windowLength,searchRange);
            filteredBand = new float[searchRange * 2+1];
        }
        public void updateWindow(int Width, int Height){
            this.mDrawingHeight = Height;
            this.mDrawingWidth = Width;
        }
        public void quit(){
            isDrawing = false;
        }
        public void notifyDataChange(short[] data){
            this.data = data;
            isRefresh = true;
        }

        public void enableLog(){
            isLog = true;
        }

        @Override
        public void run() {
            super.run();
            //draw(fftResult);
            while (isDrawing){
                if(isRefresh){
                    isRefresh = false;
                    pcmData = data;
                    //the following is fft result
                    fft.complexLization(complexData,pcmData);
                    fft.FFT(complexData);
                    fft.magnitude(complexData,fftResult);
                    draw(fftResult);

                    //the following draw the original acoustic wave
                    //drawLine(pcmData);
                    //Log.d(DrawEvent.class.getSimpleName(),"--------get notified message");
                }
            }
        }

        private void drawLine(short[] data){
            try {
                float scale = 1;
                int axis = mDrawingHeight / 2;
                int fresh = data.length / mDrawingWidth;
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);
                for (int i = 0; i < fresh; i++) {
                    switch (i){
                        case 0: mPaint.setColor(Color.RED);break;
                        case 1: mPaint.setColor(Color.GREEN);break;
                        case 2: mPaint.setColor(Color.WHITE);break;
                    }
                    for (int j = 1; j < mDrawingWidth; j++) {
                        mCanvas.drawLine(j - 1, data[i * fresh + j - 1] * scale + axis, j, data[i * fresh + j]*scale + axis, mPaint);
                    }
                    //mCanvas.drawColor(Color.BLACK);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        /**
         * draw the fft result;
         * @param data
         */
        private void draw(float[] data){
            try {

                System.arraycopy(data,0,fftHalf,0,fftHalf.length);

                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);
                //mCanvas.drawLine(100,100,200,300,mPaint);

                //shift = data.length/2 - mDrawingWidth;
                //y = data.length / 2;

                shift = 0;
                y = mDrawingWidth > fftHalf.length ? fftHalf.length : mDrawingHeight;
                for(int i = shift; i< y;i++){
                    mCanvas.drawLine(i-shift,mDrawingHeight,i-shift,(mDrawingHeight-fftHalf[i]*mDrawingHeight/32768),mPaint);
                }
                peak = findPeak(fftHalf);
                frequency = peak * deltF;
                if(peak > mDrawingWidth){
                    peak = mDrawingWidth;
                }
                mCanvas.drawText(String.valueOf(frequency),peak+5,mDrawingHeight/2,textPaint);

                System.arraycopy(fftHalf,peak - searchRange,filteredBand,0,filteredBand.length);
                //movingAverage.add(filteredBand);
                //searchEcho(peak,fftHalf);
                //Log.d(">>>",movingAverage.getAverage().toString());
                if(isLog){
                    isLog = false;
                    Log.d(TAG,">>>>>"+filteredBand.toString());
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        private int findPeak(float[] data){
            float max = 0;
            int index = 0;
            for(int i = 300; i<data.length;i++){
                if(data[i] > max){
                    max = data[i];
                    index = i;
                }
            }
            return index;
        }

        private void searchEcho(int peak,float[] data){
            int range = 20;
            int threshold = 50;
            int lowband = 0;
            int highband = 0;
            for(int i = 0; i < range ; i++){
                if(data[peak - i] > threshold){
                    lowband ++;
                }
                if(data[peak + i] > threshold){
                    highband ++;
                }
            }
            Log.d(TAG,"Lowband = "+lowband + " Highband="+highband);
        }
        private int searchEcho(float centerFrequency){


            return 0;
        }
    }

    private class MovingAverage{
        private List<float[]> queue;
        private int size;
        private int searchRange;
        private List<Float> average;
        private float[] tmpData;
        private int curIndex = 0;
        private boolean isFilled = false;
        public MovingAverage(int n, int searchRange){
            this.size = n; // data matrix vector size
            this.searchRange = searchRange; // data matrix type size
            queue = new ArrayList<>();
            average = new ArrayList<Float>(2 * searchRange + 1);
            for(int i = 0 ; i < 2 * searchRange + 1 ; i++){
                float[] data = new float[n];
                queue.add(data);
                average.add(0f);
            }

            //tmpData = new float[2*searchRange + 1];

            Log.i(TAG,"!!!!!!!!!!!!!!quequ size="+queue.size());
            Log.i(TAG,"!!!!!!!!!!!!!!list size="+average.size());
        }

        public void add(float[] data){
            for(int i = 0;i<queue.size(); i++) {
                tmpData = queue.get(i);
                tmpData[curIndex] = data[i];
            }
            curIndex++;
            if (curIndex >= this.size){
                curIndex = 0;
                isFilled = true;
            }
        }

        public List<Float> getAverage(){
            int averageIdx = 0;
            float sum = 0;
            if(isFilled)
                averageIdx = size;
            else
                averageIdx = curIndex;
            for(int i = 0 ; i < queue.size() ; i ++){
                tmpData = queue.get(i);
                for(int j = 0;j< tmpData.length; j++){
                    sum += tmpData[j];
                }
                average.set(i,sum / averageIdx);
            }

            return average;
        }
    }
}
