package hust.cc.acoustic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;

public class CoherentRecieverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coherent_reciever);
        ButterKnife.bind(this);

    }
}
