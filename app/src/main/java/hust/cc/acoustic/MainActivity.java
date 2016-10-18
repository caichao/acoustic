package hust.cc.acoustic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.util.AudioPlayerThread;
import hust.cc.acoustic.util.AudioRecorder;
import hust.cc.acoustic.util.DrawEvent;
import hust.cc.acoustic.util.PlayToneThread;
import in.excogitation.zentone.library.ToneStoppedListener;
import in.excogitation.zentone.library.ZenTone;


public class MainActivity extends AppCompatActivity{

    //***********************vars about ui part********************************
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.btn_record)
    Button btnRecord;

    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceOn = false;
    private DrawEvent drawEvent;
    //***********************non-ui part vars*********************************
    private AudioPlayerThread mAudioPlayerThread;
    private AudioRecorder mAudioRecorder;
    private float[] fft;
    private static final String TAG = "MainActivity";

    private static final int AUDIO_PLAY_STOPPED = 1;
    private boolean isAudioPlayed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initUI();
        initParams();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private PlayToneThread playToneThread = new PlayToneThread(20000,1.0f);
    @OnClick(R.id.button)
    public void onClickAudioPlay() {
            isAudioPlayed = true;
        //if (button.getText().equals(getString(R.string.button_text_play))) {
            /*Toast.makeText(getApplicationContext(),"start audio",Toast.LENGTH_SHORT).show();
            ZenTone.getInstance().generate(20000, 10, 1, new ToneStoppedListener() {
                @Override
                public void onToneStopped() {
                    Message msg = myHandler.obtainMessage();
                    msg.what = AUDIO_PLAY_STOPPED;
                    msg.sendToTarget();
                }
            });*/
            if(!playToneThread.isAlive()){
                playToneThread.enablePlay();
                playToneThread.start();
                Toast.makeText(getApplicationContext(),"start audio",Toast.LENGTH_SHORT).show();
            }
            //button.setText(getString(R.string.button_text_pause));
        //}
        //
        /*else {
            if (mAudioPlayerThread != null) {
                mAudioPlayerThread.Pause();
            }
            button.setText(getString(R.string.button_text_play));
        }*/
    }

    @OnClick(R.id.btn_record)
    public void onClickAudioRecord() {
        if(btnRecord.getText().equals(getString(R.string.button_text_record))){
            if(mAudioRecorder != null){
                mAudioRecorder.startRecord();
            }
            btnRecord.setText(getString(R.string.button_text_pause));
        }else {
            if(mAudioRecorder != null){
                //mAudioRecorder.stopRecord();

            }
            btnRecord.setText(getString(R.string.button_text_record));
        }
    }
    //*******************************ui related****************************************
    public void initUI() {
        drawEvent = new DrawEvent();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(drawEvent);
    }
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case AUDIO_PLAY_STOPPED:
                    Toast.makeText(getApplicationContext(),"Audio Play over",Toast.LENGTH_SHORT).show();
                    ZenTone.getInstance().generate(20000, 10, 1.0f, new ToneStoppedListener() {
                        @Override
                        public void onToneStopped() {
                            if(isAudioPlayed = true) {
                                Message message = myHandler.obtainMessage();
                                message.what = AUDIO_PLAY_STOPPED;
                                message.sendToTarget();
                            }
                        }
                    });
                    break;

            }
        }
    };
    //******************************non-ui part*************************************

    public void initParams() {
        initAudio();
        initAudioRecord();
    }

    public void initAudio() {
        mAudioPlayerThread = new AudioPlayerThread();
        mAudioPlayerThread.start();
    }

    public void initAudioRecord() {
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.recordingCallback(drawEvent);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAudioPlayerThread.Pause();
        mAudioRecorder.finishRecord();
        isSurfaceOn = false;
        isAudioPlayed = false;
        playToneThread.stopTone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAudioPlayerThread != null)
            mAudioPlayerThread.close();
    }

}
