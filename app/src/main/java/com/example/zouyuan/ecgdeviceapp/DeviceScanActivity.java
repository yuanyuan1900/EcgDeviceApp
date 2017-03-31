package com.example.zouyuan.ecgdeviceapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class DeviceScanActivity extends BaseActivity implements View.OnClickListener {
    private List<EcgDevice> ecgDeviceList = new ArrayList<>();
    private Button scanButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        EcgDeviceAdapter ecgDeviceAdapter = new EcgDeviceAdapter
                (DeviceScanActivity.this,R.layout.device_item_layout,ecgDeviceList);
        ListView listView = (ListView)findViewById(R.id.ilv_device_list);
        listView.setAdapter(ecgDeviceAdapter);

        scanButton = (Button) findViewById(R.id.ibt_scan_device);
        scanButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibt_scan_device:
            {
                Intent intent = new Intent(DeviceScanActivity.this,DeviceControlActivity.class);
                startActivity(intent);
                break;
            }

            default:
                break;
        }

    }
}
