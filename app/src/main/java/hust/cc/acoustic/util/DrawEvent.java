package hust.cc.acoustic.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import hust.cc.acoustic.R;
import hust.cc.acoustic.computation.Complex;
import hust.cc.acoustic.computation.FFT;

/**
 * Created by cc on 2016/10/13.
 */

public class DrawEvent implements SurfaceHolder.Callback, AudioRecorder.RecordingCallback {

    private int Width = 0;
    private int Height = 0;
    private DrawingThread mThread;

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
            //Log.d(TAG,"valid data length = "+bytelen); output 2048
        }
    }

    private static class DrawingThread extends Thread{
        private SurfaceHolder mSurfaceHolder;
        private int mDrawingWidth,mDrawingHeight;
        private Paint mPaint;
        private Paint textPaint;
        private short[] data;
        private Canvas mCanvas;
        private boolean isDrawing = true;
        private boolean isRefresh = false;

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
                    peak = mDrawingWidth - 100;
                }
                mCanvas.drawText(String.valueOf(frequency),peak+5,mDrawingHeight/2,textPaint);

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
    }
}
