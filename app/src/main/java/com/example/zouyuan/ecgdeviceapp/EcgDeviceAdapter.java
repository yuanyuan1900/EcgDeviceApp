package com.example.zouyuan.ecgdeviceapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zouyuan on 2017/3/31.
 */

public class EcgDeviceAdapter extends ArrayAdapter<EcgDevice> {

    private  int resourceId;

    public EcgDeviceAdapter(Context context, int resource, List<EcgDevice> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        EcgDevice ecgDevice = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.deviceNameTV = (TextView)view.findViewById(R.id.itv_device_name);
            viewHolder.deviceAddressTV = (TextView) view.findViewById(R.id.itv_device_address);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceNameTV.setText(ecgDevice.getDeviceName());
        viewHolder.deviceAddressTV.setText(ecgDevice.getDeviceAddress());
        return view;
    }
    class ViewHolder{
        TextView deviceNameTV;
        TextView deviceAddressTV;
    }
}
