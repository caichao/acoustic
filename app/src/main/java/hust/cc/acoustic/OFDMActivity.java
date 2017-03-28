package hust.cc.acoustic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    private Toast mToast;

    //variables


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofdm);
        ButterKnife.bind(this);
    }

    private void initUI(){
        mToast = null;
    }
    private void initParameters(){

    }

    //************handle on click event*********************
    @OnClick(R.id.btn_send_pn)
    void sendPN(){

    }
    @OnClick(R.id.btn_send_chirp)
    void sendChirp(){

    }
    @OnClick(R.id.btn_send_zc)
    void sendZC(){

    }
    @OnClick(R.id.btn_send_zccode)
    void sendZCCode(){

    }
    @OnClick(R.id.btn_set_parameters)
    void setParameter(){

    }


}
