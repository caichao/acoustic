package hust.cc.acoustic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hust.cc.acoustic.communication.CommSocket;
import hust.cc.acoustic.util.AudioRecorder;

public class SampleActivity extends AppCompatActivity implements  AudioRecorder.RecordingCallback{

    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.btn_start_send)
    Button btnStartSend;

    private CommSocket commSocket;
    private AudioRecorder mAudioRecorder;
    private boolean isAllowedSend;

    private String IP_ADDRESS = "IP_ADDRESS";
    private String ADDR_PORT = "ADDR_PORT";
    private String SETTING = "SETTING";
    private String TAG = SampleActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                settingDialog();
                fab.hide();
            }
        });

        initParams();
    }

    //******************************** on click event*************************************
    @OnClick(R.id.btn_start_send)
    public void startSendDataToServer(){
        isAllowedSend = true;
        Toast.makeText(getApplicationContext(),"start send data to server",Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_play)
    public void startPlay(){
        mAudioRecorder.startRecord();
        Toast.makeText(getApplicationContext(),"start to play",Toast.LENGTH_SHORT).show();
    }

    //********************************* init **********************************************
    private void initParams(){
        commSocket = CommSocket.getInstance();
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.recordingCallback(this);

        isAllowedSend = false;
    }

    //************************************ ui part *******************************************
    private void settingDialog(){
        LayoutInflater layoutInflater = getLayoutInflater();
        View layout = layoutInflater.inflate(R.layout.layout_setting,null);
        final EditText ip = (EditText)layout.findViewById(R.id.ip_address);
        final EditText port = (EditText)layout.findViewById(R.id.ip_port);

        SharedPreferences setting = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        String ipAddr = setting.getString(IP_ADDRESS," ");
        int ipPort = setting.getInt(ADDR_PORT,-1);

        ip.setText(ipAddr);
        port.setText(String.valueOf(ipPort));

        new AlertDialog.Builder(this)
                .setTitle("Set IP address and port")
                .setView(layout)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ipAddress = ip.getText().toString();
                        int ipPort = Integer.parseInt(port.getText().toString());
                        if(commSocket != null){
                            commSocket.setup(ipAddress,ipPort);
                            commSocket.start();
                            Toast.makeText(getApplicationContext(),"Init Socket ok",Toast.LENGTH_SHORT).show();
                        }

                        SharedPreferences setting = getSharedPreferences(SETTING,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = setting.edit();
                        editor.putString(IP_ADDRESS,ipAddress);
                        editor.putInt(ADDR_PORT,ipPort);
                        editor.commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }

    //******************************************************************************************

    //********************************* overide method **************************************************
    @Override
    protected void onStop() {
        super.onStop();
        if(commSocket != null){
            commSocket.close();
        }
    }


    @Override
    public void onDataReady(short[] data, int bytelen) {

        //Log.d(TAG,"data length = "+bytelen);
        if(isAllowedSend){
            try {
                commSocket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
