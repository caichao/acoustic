package hust.cc.acoustic.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import hust.cc.acoustic.computation.MathCC;
import hust.cc.acoustic.computation.Spectrum;

/**
 * Created by cc on 2017/9/28.
 */

public class SpectrumGraph implements SurfaceHolder.Callback, AudioRecorder.RecordingCallback{
    private int Width = 0;
    private int Height = 0;
    private DrawingThread mThread;
    private boolean enableLog = false;
    private int fs = 48000; // a default value


    boolean isTimeToFresh = false;

    public SpectrumGraph(int fs){
        this.fs = fs;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mThread = new DrawingThread(surfaceHolder, fs);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Width = i1;
        Height = i2;
        mThread.updateWindow(Width,Height);
        Log.d(SpectrumGraph.class.getSimpleName(),"-------------------Width="+Width);
        Log.d(SpectrumGraph.class.getSimpleName(),"-------------------Height="+Height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mThread.quit();
        if(mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    @Override
    public void onDataReady(short[] data, int bytelen) {
        if(mThread != null) {
            //mThread.drawBar((int)(Math.random()* Width), (int)(Math.random()* Height));
            mThread.loadData(data);
            mThread.enableFresh();
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
        //parameters to draw the line
        private Paint mPaint;
        private Paint textPaint;
        private Canvas mCanvas;

        // scale the window
        private int mDrawingWidth,mDrawingHeight;
        private double scaleRatioHeight = 1.0;
        private int scaleRatioWidth = 1;
        private int fs = 48000;
        private int MaxHeight = 0;

        private SurfaceHolder mSurfaceHolder;

        boolean isDrawing = true;
        private StringBuilder logBuilder;
        private String logInfo;

        private double[] spectrum = null;
        Spectrum mSpectrum = null;
        private double[] signal = null;

        //parameter to normalize the data
        private double maxValue = 1;
        private double ratio = 2;
        private int decimation = 10;

        private  int i = 0;
        //
        boolean isTimeToFresh = false;

        public DrawingThread(SurfaceHolder mSurfaceHolder, int fs){
            this.mSurfaceHolder = mSurfaceHolder;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(2.0f);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setStrokeWidth(2.0f);

            this.fs = fs;

            logBuilder = new StringBuilder();
            logInfo = new String();

            //initial audio samples
            spectrum = new double[fs/2];
            signal = new double[fs];

            spectrum = new double[fs/2];
            mSpectrum = new Spectrum(fs, fs);
        }

        public void updateWindow(int Width, int Height){
            this.mDrawingHeight = Height;
            this.mDrawingWidth = Width;
            this.scaleRatioWidth = fs/mDrawingWidth;
            MaxHeight = mDrawingHeight;// the default maxHeight is the graph height
        }

        public void setMaxHeight(int height){
            MaxHeight = height;
        }

        /**
         * load the new audio data
         * @param x: the input of the audio data
         */
        public void loadData(short[] x){
            for(i = 0; i < x.length; i++){
                signal[i] = x[i];
            }
        }

        /**
         * called by the upper layer to fresh the surface view
         */
        public void enableFresh(){
            synchronized (this){
                isTimeToFresh = true;
            }
        }

        public void UpdateSpectrum(double[] m){
            //System.arraycopy(m, 0, signal, 0, m.length);
            mSpectrum.fft(m);
            spectrum = mSpectrum.getFreqResponse();
            maxValue = MathCC.getMax(spectrum);
            ratio = maxValue / Height;
            for(i = 0; i < spectrum.length ; i++){
                spectrum[i] = spectrum[i]/ratio;
            }
        }

        public void enableLog(){

        }

        public void quit(){
            isDrawing = false;
        }

        /**
         * this api draw a vertical line on the screen
         * This is a low lever api
         * @param x : the x coordinate
         * @param height : the height of the vertical line
         */
        public void drawBar(int x, int height){
            /*try{
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);
                //mPaint.setColor(Color.RED);
                */
                mCanvas.drawLine(x,mDrawingHeight,x,mDrawingHeight - height,mPaint);
            /*}catch (Exception e){
                e.printStackTrace();
            }finally {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }*/
        }
        public void setBarColor(int color){
            mPaint.setColor(color);
        }

        /**
         * draw the spectrum with the input range
         * @param x : the power spectrum
         * @param start : the start index for x
         * @param end : the end index for x
         */
        public void drawBars(double[] x, int start, int end){

            try{
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);

                for(i = start ; i < end ; i++){
                    drawBar(i-start, (int) x[i]);
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        @Override
        public void run() {
            super.run();
            while (isDrawing){
                if(isTimeToFresh == true){
                    UpdateSpectrum(signal);
                    drawBars(spectrum,800,800+Width);
                }
            }
        }
    }


}
