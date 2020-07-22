package com.example.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
    private static final String TAG = "MyCustomAdapter";

    private List<BluetoothDevice> list;
    private Context context;

    public MyCustomAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return list.get(pos).hashCode();
        //just return 0 if your list items do not have an Id variable.
    }

    public void loadItems(List<BluetoothDevice> list) {
        this.list = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, null);
        }

        //Handle TextView and display string from your list
        TextView tvContact= view.findViewById(R.id.deviceInfo);

        final BluetoothDevice device = list.get(position);

        String displayText = device.getName() + device.getAddress();
        tvContact.setText(displayText);

        //Handle buttons and add onClickListeners
        Button callbtn= view.findViewById(R.id.startPairBtn);

        callbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                try {
                    Log.d(TAG, "onClick: now pairing with : " + device.getName());

//                    device.createBond();

                    BluetoothSocket tmp = null;

                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                    try {
                        // Get a BluetoothSocket to connect with the given BluetoothDevice.
                        // MY_UUID is the app's UUID string, also used in the server code.
                        tmp = device.createRfcommSocketToServiceRecord(uuid);
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's create() method failed", e);
                    }

//                    mmSocket = tmp;
                    if (context instanceof MainActivity){
                        ((MainActivity)context).setBluetoothSocket(tmp);
                        ((MainActivity)context).closeAlertDialog();

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }
}
