package hust.cc.acoustic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.util.CodeGeneration;
import hust.cc.acoustic.util.PlayPCMThread;

public class OFDMActivity extends AppCompatActivity {

    //UI variables
    @BindView(R.id.btn_send_zc)
    Button mButtonZC;
    @BindView(R.id.btn_send_pn)
    Button mButtonPN;
    @BindView(R.id.btn_send_chirp)
    Button mButtonChirp;
    @BindView(R.id.btn_send_zccode)
    Button mButtonZCCode;
    @BindView(R.id.btn_set_parameters)
    Button mButtonSet;
    @BindView(R.id.edit_repeat_time)
    EditText mEditRepeatTime;
    @BindView(R.id.edit_gap_samples)
    EditText mEditGapSamples;

    @BindString(R.string.message_not_implemented)
    String mNotImplementedMessage;
    @BindString(R.string.not_enough_input)
    String mNotEnoughInput;

    private Toast mToast;

    //variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofdm);
        ButterKnife.bind(this);
        initParameters();
        initUI();
    }

    private void initUI(){
        mToast = null;
    }
    private void initParameters(){

    }

    //************handle on click event*********************
    @OnClick(R.id.btn_send_pn)
    void sendPN(){
        notImplementedMessage();
    }
    @OnClick(R.id.btn_send_chirp)
    void sendChirp(){
        //notImplementedMessage();
        new Thread(new PlayPCMThread(CodeGeneration.TypeChirp, 5,200)).start();
    }
    @OnClick(R.id.btn_send_zc)
    void sendZC(){
        //notImplementedMessage();
        new Thread(new PlayPCMThread(CodeGeneration.TypeZC, 1,0)).start();
    }

    @OnClick(R.id.btn_send_zccode)
    void sendZCCode(){
        notImplementedMessage();
    }
    @OnClick(R.id.btn_set_parameters)
    void setParameter(){
        String tmpRepeat = mEditRepeatTime.getText().toString();
        String tmpGap = mEditGapSamples.getText().toString();
        if(!tmpRepeat.equals("") && !tmpGap.equals("")){
            int repeat = Integer.parseInt(tmpRepeat);
            int gap = Integer.parseInt(tmpGap);
            new Thread(new PlayPCMThread(CodeGeneration.TypeChirp, repeat, gap)).start();
        }else {
            if(mToast != null)
                mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(),mNotEnoughInput,Toast.LENGTH_LONG);
            mToast.show();
        }
    }

    private void notImplementedMessage(){
        if(mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(getApplicationContext(),mNotImplementedMessage, Toast.LENGTH_LONG);
        mToast.show();
    }
}
