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

public class MainActivity extends AppCompatActivity implements AudioRecorder.CallBack {

    //***********************vars about ui part********************************
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.btn_record)
    Button btnRecord;

    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceOn = false;
    //***********************non-ui part vars*********************************
    private AudioPlayerThread mAudioPlayerThread;
    private AudioRecorder mAudioRecorder;
    private float[] fft;

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
                mAudioRecorder.stopRecord();
            }
            btnRecord.setText(getString(R.string.button_text_record));
        }
    }
    //*******************************ui related****************************************
    public void initUI() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new DrawEvent());
    }

    @Override
    public void onDataReceived(float[] pcmData, int validLength) {
        synchronized (fft) {
            fft = pcmData;
        }
    }



    private class DrawEvent implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            isSurfaceOn = true;
        }

        @Override
        public void surfaceChanged(final SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            final Paint p = new Paint();
            final int Width = i1;
            final int Height = i2;
            new Thread() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();

                    while (isSurfaceOn && fft != null) {
                        if (surfaceHolder == null) return;
                        Canvas c = surfaceHolder.lockCanvas(new Rect(0, 0, Width, Height));
                        c.drawColor(Color.BLACK);
                        p.setStrokeWidth(2);
                        p.setColor(Color.RED);

                        for (int i = 1; i < fft.length; i++) {
                            p.setColor(Color.RED);
                            c.drawLine(Width / 6 + fft[i], i - 1, Width / 6 + fft[i], i, p);
                        }
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            isSurfaceOn = false;
        }
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
        mAudioRecorder = new AudioRecorder(this);
        mAudioRecorder.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAudioPlayerThread.Pause();
        mAudioRecorder.close();
        isSurfaceOn = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAudioPlayerThread != null)
            mAudioPlayerThread.close();
    }

}
