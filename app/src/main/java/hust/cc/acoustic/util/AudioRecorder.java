package hust.cc.acoustic.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import hust.cc.acoustic.computation.Complex;
import hust.cc.acoustic.computation.FFT;

/**
 * Created by cc on 2016/10/12.
 */

public class AudioRecorder extends Thread{

    private AudioRecord mAudioRecord;
    private Context mContext;
    private int miniSize = 0;
    private boolean isRecording = false;
    private boolean isClose = false;
    private boolean isFFTFormat = false;
    private boolean isInitialOk = false;
    private CallBack mCallBack;

    private static final String TAG = "AudioRecorder";

    public AudioRecorder(Context mContext){
        this.mContext = mContext;
        init();
    }

    public AudioRecorder(){
        init();
    }

    private void init(){
        miniSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);//(AudioManager.STREAM_MUSIC);
        miniSize = miniSize > 1024 ? 1024:miniSize;

        Log.d(TAG,"----------setting buffer size is(miniSize) = "+miniSize);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, miniSize);
        if(mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED)
        {
            Log.d(TAG,"--------------Initialize AudioRecord success");
        }
        else
        {
            Log.e(TAG,"--------------Initialize AudioRecord Error");
        }
        isInitialOk = true;
    }

    public void setFFTFormat(){
        isFFTFormat = true;
    }
    public void setRawFormat(){
        isFFTFormat = false;
    }

    public void startRecord(){
        isRecording = true;
    }
    public void stopRecord(){
        isRecording = false;
    }
    public void close(){
        isClose = true;
        if(mAudioRecord != null){
            mAudioRecord.release();
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            while (!isInitialOk);
            mAudioRecord.startRecording();
            short[] pcmData = new short[miniSize];
            float[] fftResult = new float[miniSize];
            FFT fft = new FFT(miniSize);
            Complex[] complexData = new Complex[miniSize];
            for(int i = 0;i<complexData.length;i++){
                complexData[i] = new Complex();
            }
            int readLength = 0;
            while (true){
                if(isRecording){
                    readLength = mAudioRecord.read(pcmData,0,miniSize);

                    if(isFFTFormat){
                        fft.complexLization(complexData,pcmData);
                        fft.FFT(complexData);
                        fft.magnitude(complexData,fftResult);
                        mCallBack.onDataReceived(fftResult,readLength);
                    }else {
                        //mCallBack.onDataReceived(pcmData,readLength);
                    }
                }
                if(isClose){
                    break;
                }
            }

            mAudioRecord.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static interface CallBack{
        void onDataReceived(float[] pcmData, int validLength);
    }
}
