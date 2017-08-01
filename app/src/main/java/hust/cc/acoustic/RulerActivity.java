package hust.cc.acoustic;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.communication.CommSocket;
import hust.cc.acoustic.util.AudioRecorder;

public class RulerActivity extends AppCompatActivity implements AudioRecorder.RecordingCallback {


    private String TAG = RulerActivity.class.getSimpleName();

    @BindView(R.id.btn_set_parameters)
    Button mButtonConfirm;
    @BindView(R.id.fmin_input)
    EditText mFminInput;
    @BindView(R.id.bandwidth_input)
    EditText mBandwidthInput;
    @BindView(R.id.period_input)
    EditText mPeriodInput;
    @BindView(R.id.velocity_input)
    EditText mVelocityInput;
    @BindView(R.id.distance_id)
    TextView mDistance;

    private Toast mToast = null;

    private int fmin = 18000;
    private int B = 4000;
    private float T = 0.04f;
    private float V = 346.5f;

    private String ip = "";
    private int port = 1234;

    private AudioRecorder mAudioRecorder = null;
    private CommSocket mCommSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //    .setAction("Action", null).show();
                fab.setVisibility(View.INVISIBLE);
        }
        });

        initParam();
    }

    private void initParam(){
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.recordingCallback(this);

        mCommSocket = CommSocket.getInstance();
        mCommSocket.init(ip, port);

    }

    private void ToastMessage(String message, int length){
        if(mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(getApplicationContext(),message,length);
        mToast.show();
    }

    @OnClick(R.id.btn_set_parameters)
    void onButtonConfirmClicked(){
        //begin parse the input parameters
        String tmp = mFminInput.getText().toString().trim();

    }

    @Override
    public void onDataReady(short[] data, int bytelen) {

    }
}
