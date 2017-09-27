package hust.cc.acoustic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.communication.CommSocket;
import hust.cc.acoustic.computation.DSP;
import hust.cc.acoustic.signal.SignalGenerator;
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
    @BindView(R.id.ip_address)
    EditText mIPInput;
    @BindView(R.id.port_num)
    EditText mPortInput;

    private Toast mToast = null;

    //parameters for the chirp signal
    private int fmin = 18000;
    private int B = 4000;
    private float T = 0.04f;
    private float V = 346.5f;
    private int fs = 48000;

    //parameters to warm up the speaker
    private int warnUpCount = 1;
    private int warnUpThreshold = 20;

    //parameters for mixing
    private short[] x = null;
    private SignalGenerator signalGenerator = null;

    @BindString(R.string.hint_error)
    String errMsg;
    @BindString(R.string.hint_sucess)
    String successMsg;
    @BindString(R.string.hint_input)
    String inputErrorMsg;
    @BindString(R.string.toast_start_recording)
    String startRecordingToast;

    private Boolean isSocketInitOK = false;

    //private String ip = "";
    //private int port = 1234;

    private String IP_ADDRESS = "IP_ADDRESS";
    private String ADDR_PORT = "ADDR_PORT";
    private String SETTING = "SETTING";

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
                if(initSocket() == true){
                    fab.setVisibility(View.INVISIBLE);
                    isSocketInitOK = true;
                    ToastMessage(successMsg, Toast.LENGTH_LONG);
                }
        }
        });
        initUI();
        initParam();

    }

    private void initParam(){
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.recordingCallback(this);

        //mCommSocket = CommSocket.getInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCommSocket != null){
            mCommSocket.close();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void initUI()
    {
        SharedPreferences setting = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        String ipAddr = setting.getString(IP_ADDRESS," ").trim();
        int ipPort = setting.getInt(ADDR_PORT,-1);
        mIPInput.setText(ipAddr);
        mPortInput.setText(ipPort+"");
    }

    /**
     * used to get the ip address and port number from the input
     * @return   false indicates error
     *            true indicates success
     */
    private boolean initSocket(){
        String IPStr = mIPInput.getText().toString().trim();
        String portStr = mPortInput.getText().toString().trim();

        if(IPStr == null || portStr == null){
            ToastMessage(errMsg, Toast.LENGTH_LONG);
            return false;
        }
        if(IPStr.equals("") || portStr.equals("")){
            ToastMessage(inputErrorMsg,Toast.LENGTH_LONG);
            return false;
        }

        int tmp = Integer.parseInt(portStr);
        Log.d(TAG, "ip = " + IPStr + " port = " + portStr );
        mCommSocket.init(IPStr,tmp);
        mCommSocket.start();

        SharedPreferences setting = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putString(IP_ADDRESS,IPStr);
        editor.putInt(ADDR_PORT,tmp);
        editor.commit();

        return true;
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
        //String tmp = mFminInput.getText().toString().trim();
        signalGenerator = new SignalGenerator(fs, B, T, fmin);
        x = signalGenerator.generateChirp();

        ToastMessage(startRecordingToast,Toast.LENGTH_SHORT);
        mAudioRecorder.startRecord();
    }

    @Override
    public void onDataReady(short[] data, int bytelen) {
        //Log.e(TAG, ""+bytelen);
        if(warnUpCount < warnUpThreshold){
            warnUpCount++;
        }else {
            try {

                Log.e(TAG, "xcorr"+DSP.xcorr(data, x));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                //isSocketInitOK = false;
                //mCommSocket.close();
            }

        }
    }
}
