package com.example.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn, mScanBtn;

    BluetoothAdapter mBlueAdapter;
    BroadcastReceiver mReceiver, mPairReceiver;
    BluetoothSocket mmSocket;

    ListView mListView;

    List<BluetoothDevice> mAvailableDevices;
    MyCustomAdapter mCustomAdapter;

    NestedScrollView mScrollView;

    AlertDialog mAlertDialog;

    private Handler handler; // handler that gets info from Bluetooth service

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mScrollView = findViewById(R.id.scrollView);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv     = findViewById(R.id.pairedTv);
        mBlueIv       = findViewById(R.id.bluetoothIv);
        mOnBtn        = findViewById(R.id.onBtn);
        mOffBtn       = findViewById(R.id.offBtn);
        mDiscoverBtn  = findViewById(R.id.discoverableBtn);
        mPairedBtn    = findViewById(R.id.pairedBtn);
        mScanBtn      = findViewById(R.id.scanBtn);
//        mListView     = findViewById(R.id.listView);
        mCustomAdapter = new MyCustomAdapter( this);
//        mListView.setAdapter(mCustomAdapter);

        mAvailableDevices = new ArrayList<>();
        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mPairedTv.setText(mPairedTv.getText() + "\n" + msg.toString());
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        };

        //check if bluetooth is available or not
        if (mBlueAdapter == null){
            mStatusBlueTv.setText("Bluetooth is not available");
        }
        else {
            mStatusBlueTv.setText("Bluetooth is available");
        }

        //set image according to bluetooth status(on/off)
        if (mBlueAdapter.isEnabled()){
            mBlueIv.setImageResource(R.drawable.bluetooth_on);
        }
        else {
            mBlueIv.setImageResource(R.drawable.bluetooth_off);
        }

        //on btn click
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPairedTv.setText("");
                if (!mBlueAdapter.isEnabled()){
                    showToast("Turning On Bluetooth...");
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else {
                    showToast("Bluetooth is already on");
                }
            }
        });

        //discover bluetooth btn click
        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPairedTv.setText("");
                if (!mBlueAdapter.isDiscovering()){
                    showToast("Making Your Device Discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });
        //off btn click
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPairedTv.setText("");
                if (mBlueAdapter.isEnabled()){
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth Off");
                    mBlueIv.setImageResource(R.drawable.bluetooth_off);
                }
                else {
                    showToast("Bluetooth is already off");
                }
            }
        });
        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDevice: " + device.getName() + ", " + device);
                    }

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View convertView = inflater.inflate(R.layout.listview, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Paired Devices");
                    ListView lv = convertView.findViewById(R.id.listView1);

                    lv.setAdapter(mCustomAdapter);
                    mAlertDialog = alertDialog.show();

                    List<BluetoothDevice> pairedDevices = new ArrayList<>();
                    pairedDevices.addAll(devices);

                    mCustomAdapter.loadItems(pairedDevices);
                    mCustomAdapter.notifyDataSetChanged();
                } else {

                    // scan available devices

                    mScanBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                mPairedTv.setText("Scanning Available Devices...");

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View convertView = inflater.inflate(R.layout.listview, null);
                            alertDialog.setView(convertView);
                            alertDialog.setTitle("Scanning");
                            ListView lv = convertView.findViewById(R.id.listView1);

                            lv.setAdapter(mCustomAdapter);
                            mAlertDialog = alertDialog.show();

                            mBlueAdapter.startDiscovery();
                            mReceiver = new BroadcastReceiver() {
                                public void onReceive(Context context, Intent intent) {
                                    String action = intent.getAction();

                                    //Finding devices
                                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                        Log.d(TAG, "onReceive: just got something");
                                        // Get the BluetoothDevice object from the Intent
                                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                                        if (device != null) {

                                            mAvailableDevices.add(device);
//                                mPairedTv.setText(mPairedTv.getText() +  "\n\n" + device.getName() + "\n" + device.getAddress());

                                            mCustomAdapter.loadItems(mAvailableDevices);
                                            mCustomAdapter.notifyDataSetChanged();

                                            Log.d(TAG, "onReceive: " + device.getName());
                                            Log.d(TAG, "onReceive: " + device.getAddress());

                                        }
                                    }
                                }

                            };

                            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            registerReceiver(mReceiver, filter);


                            mPairReceiver = new BroadcastReceiver() {
                                public void onReceive(Context context, Intent intent) {
                                    String action = intent.getAction();

                                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                                        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                                        final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                                        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                                            showToast("Paired");
                                        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                                            showToast("Unpaired");
                                        }

                                    }
                                }
                            };

                            IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                            registerReceiver(mPairReceiver, intent);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    //bluetooth is on
                    mBlueIv.setImageResource(R.drawable.bluetooth_on);
                    showToast("Bluetooth is on");
                }
                else {
                    //user denied to turn bluetooth on
                    showToast("could't on bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        unregisterReceiver(mPairReceiver);

        if (mmSocket != null) {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
                e.printStackTrace();
            }
        }
    }


    public void setBluetoothSocket(BluetoothSocket tmpSocket) {
        Log.d(TAG, "setBluetoothSocket: setting socket");
        this.mmSocket = tmpSocket;

        this.connectToBluetoothSocket();
    }

    public void closeAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
    }

    protected void connectToBluetoothSocket() {
        Log.d(TAG, "connectToBluetoothSocket: connecting socket");
        mPairedTv.setText("connecting socket");
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        // Cancel discovery because it otherwise slows down the connection.
        mBlueAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            Log.d(TAG, "connectToBluetoothSocket: connected successfully");
            mPairedTv.setText(mPairedTv.getText() + "\nconnected successfully");
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        } catch (IOException connectException) {
            Log.d(TAG, "connectToBluetoothSocket: threw io exception");
            mPairedTv.setText(mPairedTv.getText() + "\nthrew io exception");
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
            connectException.printStackTrace();
            // Unable to connect; close the socket and return.
            try {
                Log.d(TAG, "connectToBluetoothSocket: trying to close connection");
                mPairedTv.setText(mPairedTv.getText() + "\ntrying to close connection");
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
                mPairedTv.setText(mPairedTv.getText() + "\nCould not close the client socket");
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket();
        showToast("The connection attempt succeeded");
    }

    protected void manageMyConnectedSocket() {
        new ConnectedThread(mmSocket).start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    Log.d(TAG, "run: numBytes : " + numBytes);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MainActivity.MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MainActivity.MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MainActivity.MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}