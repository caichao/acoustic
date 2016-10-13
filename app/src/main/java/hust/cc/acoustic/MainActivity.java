package hust.cc.acoustic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.util.AudioPlayerThread;
import hust.cc.acoustic.util.AudioRecorder;
import hust.cc.acoustic.util.DrawEvent;

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

    @OnClick(R.id.button)
    public void onClickAudioPlay() {
        if (button.getText().equals(getString(R.string.button_text_play))) {
            if (mAudioPlayerThread != null) {
                mAudioPlayerThread.play();
            }
            button.setText(getString(R.string.button_text_pause));
        } else {
            if (mAudioPlayerThread != null) {
                mAudioPlayerThread.Pause();
            }
            button.setText(getString(R.string.button_text_play));
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAudioPlayerThread != null)
            mAudioPlayerThread.close();
    }

}
