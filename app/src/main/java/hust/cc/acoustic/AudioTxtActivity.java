package hust.cc.acoustic;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.util.AudioRecorder;

public class AudioTxtActivity extends AppCompatActivity implements AudioRecorder.RecordingCallback{


    //UI variable
    private Toast mToast = null;
    @BindView(R.id.button_start)
    Button mStartButton ;
    @BindView(R.id.button_save)
    Button mSaveButton ;
    @BindView(R.id.textview_filename)
    EditText mFileName ;
    @BindView(R.id.progressBar)
    ProgressBar mProgress;
    @BindView(R.id.button_set)
    Button mButtonSet;
    @BindView(R.id.textview_seconds)
    EditText mSetSeconds;

    @BindString(R.string.toast_start_recording)
    String mToastRecording;
    @BindString(R.string.toast_start_save)
    String mToastSaving;
    @BindString(R.string.toast_record_over)
    String mToastRecordingOver;
    @BindString(R.string.toast_save_complete)
    String mToastSaveComplete;
    @BindString(R.string.hint_no_input)
    String mMessageNoInput;
    @BindString(R.string.toast_set_time)
    String mToastSetTimeOk;


    // Non UI variable
    private String TAG = AudioTxtActivity.class.getSimpleName();
    private short[] mPCM;
    private int mCurrentBytes = 0;
    private int mProgressIndex = 0;
    private int TotalBytes = 48000 * 10; // Record the audio signal for 10 seconds
    private boolean isAllowRecording = false;
    private AudioRecorder mAudioRecorder;
    public static final String SDPATH = Environment.getExternalStorageDirectory()+ File.separator;//"/sdcard/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_txt);
        ButterKnife.bind(this);
        initUIParam();
        initParams();
    }

    private void initUIParam(){
        mToast = new Toast(getApplicationContext());
        mProgress.setMax(100);
        mProgress.setProgress(0);
    }
    private void initParams(){
        mPCM = new short[TotalBytes];
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.recordingCallback(this);
    }

    private void arrayInitial(short[] bytes, int var){
        for(int i = 0; i < bytes.length ; i++){
            bytes[i] = (short)var;
        }
    }

    private void ToastMessage(String message, int length){
        if(mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(getApplicationContext(),message,length);
        mToast.show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPCM = null;
        if(mAudioRecorder != null){
            mAudioRecorder.finishRecord();
        }
    }

    @OnClick(R.id.button_set)
    void OnTimeSetButtonClicked(){

        String timeStr = null;
        timeStr = mSetSeconds.getText().toString();
        if(timeStr != null && !timeStr.equals("") ){
            TotalBytes = Integer.parseInt(timeStr) * 48000;
            if(mPCM != null)
                mPCM = null;
            mPCM = new short[TotalBytes];
            ToastMessage(mToastSetTimeOk + "\r\n" + timeStr + " seconds",Toast.LENGTH_LONG);

        }else{
            ToastMessage(mMessageNoInput,Toast.LENGTH_LONG);
        }
    }

    @OnClick(R.id.button_start)
    void onStartRecording(){
        mCurrentBytes = 0;
        arrayInitial(mPCM, 0);
        if(!mAudioRecorder.isRecording())
            mAudioRecorder.startRecord();
        isAllowRecording = true;
        /*if(mToast != null){
            mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(),mToastRecording,Toast.LENGTH_LONG);
            mToast.show();
        } */
        ToastMessage(mToastRecording,Toast.LENGTH_LONG);
    }

    @OnClick(R.id.button_save)
    void setmSaveButton(){
        String name = null;
        name = mFileName.getText().toString();
        mProgress.setProgress(0);
        if(name != null && !name.equals("")){
            FileInfo fileInfo = new FileInfo();
            fileInfo.name = name;
            fileInfo.bytes = new short[TotalBytes];
            System.arraycopy(mPCM,0,fileInfo.bytes,0,TotalBytes);

            /*if(mToast != null){
                mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(),mToastSaving,Toast.LENGTH_SHORT);
                mToast.show();
            }*/
            ToastMessage(mToastSaving,Toast.LENGTH_LONG);

            new SaveAndUpdateUI().execute(fileInfo);
        }else {
            /*if(mToast != null){
                mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(),mMessageNoInput,Toast.LENGTH_LONG);
                mToast.show();
            }*/
            ToastMessage(mMessageNoInput,Toast.LENGTH_LONG);
        }

    }

    @Override
    public void onDataReady(short[] data, int bytelen) {
        if(isAllowRecording) {
            //Log.e(TAG, "data.length = " + data.length);
            //Log.e(TAG, "bytelen = " + bytelen);
            synchronized (data) {
                bytelen = (bytelen + mCurrentBytes) > TotalBytes ? ( TotalBytes - mCurrentBytes ) : bytelen;
                System.arraycopy(data, 0, mPCM, mCurrentBytes, bytelen);
                mCurrentBytes += bytelen;
                mProgress.setProgress((int)((1.0f * mCurrentBytes / mPCM.length) * 100));
                if (mCurrentBytes >= TotalBytes) {
                    isAllowRecording = false;
                }
            }
        }
    }

    public class FileInfo {
        public String name;
        public short[] bytes;
    }

    private class SaveAndUpdateUI extends AsyncTask<FileInfo, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            /*if(mToast != null){
                mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(),mToastSaveComplete,Toast.LENGTH_LONG);
                mToast.show();
            }*/
            ToastMessage(mToastSaveComplete,Toast.LENGTH_LONG);

            super.onPostExecute(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgress.setProgress((int) ((values[0] * 100.0f ) / TotalBytes ));
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(FileInfo... fileInfos) {
            File file = new File(SDPATH + fileInfos[0].name + ".txt");
            FileWriter fw = null;
            int bufferLength = fileInfos[0].bytes.length;
            try {
                fw = new FileWriter(file);
                if (!file.exists())
                    file.createNewFile();
                for (int i = 0; i < bufferLength; i++) {
                    fw.write(String.valueOf(fileInfos[0].bytes[i]) + "\r\n");
                    fw.flush();
                    publishProgress(i);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
