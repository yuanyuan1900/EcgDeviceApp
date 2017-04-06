package com.example.zouyuan.ecgdeviceapp;

import android.bluetooth.BluetoothDevice;
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

public class EcgDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private  int resourceId;

    public EcgDeviceListAdapter(Context context, int resource, List<BluetoothDevice> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        BluetoothDevice ecgDevice = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.deviceNameTV = (TextView)view.findViewById(R.id.itv_device_name);
            viewHolder.deviceAddressTV = (TextView) view.findViewById(R.id.itv_device_address);
            viewHolder.deviceStatusTV = (TextView)view.findViewById(R.id.itv_device_status);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceNameTV.setText(ecgDevice.getName());
        viewHolder.deviceAddressTV.setText(ecgDevice.getAddress());

        String connect_status = null;
        connect_status = getContext().getString(R.string.str_NOT_CONNECTED);
        /*if(ecgDevice.getBondState() == 1){
            connect_status = getContext().getString(R.string.str_CONNECTED);
        }else{
            connect_status = getContext().getString(R.string.str_NOT_CONNECTED);
        }*/
        viewHolder.deviceStatusTV.setText(connect_status);
        return view;
    }
    class ViewHolder{
        TextView deviceNameTV;
        TextView deviceAddressTV;
        TextView deviceStatusTV;
    }
}
