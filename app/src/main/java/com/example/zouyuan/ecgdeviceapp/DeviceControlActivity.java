package com.example.zouyuan.ecgdeviceapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by zouyuan on 2017/3/31.
 */

public class DeviceControlActivity extends BaseActivity implements View.OnClickListener {

    private Button quitAppButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        quitAppButton = (Button)findViewById(R.id.ibt_quit_app);
        quitAppButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ibt_quit_app:
                ActivityCollector.finishAll();
                break;
            default:
                break;

        }
    }
}
