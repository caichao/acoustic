package hust.cc.acoustic.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import hust.cc.acoustic.computation.Complex;
import hust.cc.acoustic.computation.FFT;

/**
 * Created by cc on 2016/10/13.
 */

public class DrawEvent implements SurfaceHolder.Callback, AudioRecorder.RecordingCallback {

    private int Width = 0;
    private int Height = 0;
    private DrawingThread mThread;
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
    public void onDataReady(short[] data) {
        //Log.d(DrawEvent.class.getSimpleName(),"-------------byte length : "+data.length);
        if(mThread != null)
            mThread.notifyDataChange(data);
    }

    private static class DrawingThread extends Thread{
        private SurfaceHolder mSurfaceHolder;
        private int mDrawingWidth,mDrawingHeight;
        private Paint mPaint;
        private short[] data;
        private Canvas mCanvas;
        private boolean isDrawing = true;
        private boolean isRefresh = false;

        private int x;
        private int y;
        private int shift;
        //FFT about
        private static final int DataSize = 4096;
        short[] pcmData ;
        float[] fftResult;
        FFT fft ;
        Complex[] complexData;


        public DrawingThread(SurfaceHolder mSurfaceHolder){
            this.mSurfaceHolder = mSurfaceHolder;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(2.0f);

            complexData = new Complex[DataSize];
            for(int i = 0;i<complexData.length;i++){
                complexData[i] = new Complex();
            }
            pcmData = new short[DataSize];
            fftResult = new float[DataSize];
            fft = new FFT(DataSize);

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
                    fft.complexLization(complexData,pcmData);
                    fft.FFT(complexData);
                    fft.magnitude(complexData,fftResult);
                    draw(fftResult);
                    //Log.d(DrawEvent.class.getSimpleName(),"--------get notified message");
                }
            }
        }

        private void draw(float[] data){
            try {
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);
                //mCanvas.drawLine(100,100,200,300,mPaint);
                shift = data.length/2 - mDrawingWidth;
                for(int i = shift; i< data.length/2;i++){
                    mCanvas.drawLine(i-shift,mDrawingHeight,i-shift,(mDrawingHeight-data[i]*mDrawingHeight/32768),mPaint);
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
