package hust.cc.acoustic.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;

import hust.cc.acoustic.computation.Complex;
import hust.cc.acoustic.computation.FFT;

/**
 * Created by cc on 2016/10/12.
 */

public class AudioRecorder implements IAudioRecorder{

    /*private AudioRecord mAudioRecord;
    //private Context mContext;
    private int miniSize = 0;
    private boolean isRecording = false;
    private boolean isClose = false;
    private boolean isFFTFormat = false;
    private boolean isInitialOk = false;
    private CallBack mCallBack;
    private static final int sampleFrequency = 44100;

    private static final String TAG = "AudioRecorder";

    public AudioRecorder(CallBack mCallBack){
        //this.mContext = mContext;
        this.mCallBack = mCallBack;
        init();
    }

    public AudioRecorder(){
        init();
    }

    private void init(){
        miniSize = AudioRecord.getMinBufferSize(sampleFrequency, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);//(AudioManager.STREAM_MUSIC);
        Log.d(TAG,"----------the minimum size buffer retreived from code: "+miniSize);
        int size = 1024;
        while(miniSize > size){
            size = size * 2;
        }
        miniSize = size;

        Log.d(TAG,"----------setting buffer size is(miniSize) = "+miniSize);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleFrequency, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, miniSize);
        if(mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED)
        {
            Log.d(TAG,"--------------Initialize AudioRecord success");
        }
        else
        {
            Log.e(TAG,"--------------Initialize AudioRecord Error");
        }
        //isInitialOk = true;
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
        if(mAudioRecord != null && mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED){
            mAudioRecord.stop();
            mAudioRecord.release();
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            //while (!isInitialOk);
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

            //mAudioRecord.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static interface CallBack{
        void onDataReceived(float[] pcmData, int validLength);
    }*/

    public static final int RECORDER_SAMPLE_RATE = 48000;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    private static final int BUFFER_BYTES_ELEMENTS = 1024;
    private static final int BUFFER_BYTES_PER_ELEMENT = RECORDER_AUDIO_ENCODING;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;


    public static final int RECORDER_STATE_FAILURE = -1;
    public static final int RECORDER_STATE_IDLE = 0;
    public static final int RECORDER_STATE_STARTING = 1;
    public static final int RECORDER_STATE_STOPPING = 2;
    public static final int RECORDER_STATE_BUSY = 3;

    private volatile int recorderState;

    private final Object recorderStateMonitor = new Object();

    private RecordingCallback recordingCallback;

    public AudioRecorder recordingCallback(RecordingCallback recordingCallback) {
        this.recordingCallback = recordingCallback;
        return this;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onRecordFailure() {
        recorderState = RECORDER_STATE_FAILURE;
        finishRecord();
    }

    @Override
    public void startRecord() {
        if (recorderState != RECORDER_STATE_IDLE) {
            return;
        }

        try {
            recorderState = RECORDER_STATE_STARTING;

            startRecordThread();
        } catch (FileNotFoundException e) {
            onRecordFailure();
            e.printStackTrace();
        }
    }

    private void startRecordThread() throws FileNotFoundException {

        new Thread(new PriorityRunnable(Process.THREAD_PRIORITY_AUDIO) {

            private void onExit() {
                synchronized (recorderStateMonitor) {
                    recorderState = RECORDER_STATE_IDLE;
                    recorderStateMonitor.notifyAll();
                }
            }


            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void runImpl() {
                int bufferSize = Math.max(BUFFER_BYTES_ELEMENTS * BUFFER_BYTES_PER_ELEMENT,
                        AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING));

                int size = 1024;
                while(size < bufferSize){
                    size = size * 2;
                }
                bufferSize = size;

                AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, bufferSize);
                if(recorder.getState() == AudioRecord.STATE_UNINITIALIZED){
                    Log.e(AudioRecorder.class.getSimpleName(),"*******************************Initialize audio recorder error");
                    return;
                }else{
                    Log.d(AudioRecorder.class.getSimpleName(),"-------------------------------Initialize AudioRecord ok");
                }
                try {
                    if (recorderState == RECORDER_STATE_STARTING) {
                        recorderState = RECORDER_STATE_BUSY;
                    }
                    recorder.startRecording();

                    short recordBuffer[] = new short[bufferSize];
                    do {
                        int bytesRead = recorder.read(recordBuffer, 0, bufferSize);

                        if (bytesRead > 0) {
                            recordingCallback.onDataReady(recordBuffer,bytesRead);
                        } else {
                            Log.e(AudioRecorder.class.getSimpleName(), "error: " + bytesRead);
                            onRecordFailure();
                        }
                    } while (recorderState == RECORDER_STATE_BUSY);
                } finally {
                    recorder.release();
                }
                onExit();
            }
        }).start();
    }

    @Override
    public void finishRecord() {
        int recorderStateLocal = recorderState;
        if (recorderStateLocal != RECORDER_STATE_IDLE) {
            synchronized (recorderStateMonitor) {
                recorderStateLocal = recorderState;
                if (recorderStateLocal == RECORDER_STATE_STARTING
                        || recorderStateLocal == RECORDER_STATE_BUSY) {

                    recorderStateLocal = recorderState = RECORDER_STATE_STOPPING;
                }

                do {
                    try {
                        if (recorderStateLocal != RECORDER_STATE_IDLE) {
                            recorderStateMonitor.wait();
                        }
                    } catch (InterruptedException ignore) {
                        /* Nothing to do */
                    }
                    recorderStateLocal = recorderState;
                } while (recorderStateLocal == RECORDER_STATE_STOPPING);
            }
        }
    }


    @Override
    public boolean isRecording() {
        return recorderState != RECORDER_STATE_IDLE;
    }

    public interface RecordingCallback {
        void onDataReady(short[] data, int bytelen);
    }


}
