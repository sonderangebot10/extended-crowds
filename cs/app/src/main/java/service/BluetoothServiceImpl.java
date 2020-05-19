package service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import interfaces.BluetoothService;
import util.CustomRequest;

public class BluetoothServiceImpl implements BluetoothService {

    private static final String NAME = "RiskSituationDetection";
    private static final String TAG = BluetoothServiceImpl.class.getName();
    private static final UUID MY_UUID = UUID.fromString("e1ce069e-55f5-4b39-99d5-a7f2a0cd7267");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Activity activity;
    private AcceptThread bluetoothServerConnection = new AcceptThread();
    private ConnectedThread connectedThread;
    private String CREATE_TASK_URL;
    private RequestQueue queue;

    public BluetoothServiceImpl(Activity activity) {

        this.activity = activity;
        this.CREATE_TASK_URL = this.activity.getApplicationContext().getString(R.string.CREATE_TASK_URL);
        this.queue = Volley.newRequestQueue(activity.getApplicationContext());
    }

    @Override
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    @Override
    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityCompat.startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null);
        }
    }

    @Override
    public Set<BluetoothDevice> findPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // 00:B5:D0:5C:A3:7A
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        ;

        bluetoothAdapter.cancelDiscovery();

        return pairedDevices;
    }

    @Override
    public void discoverDevice() {
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(receiver, filter);
    }

    @Override
    public void stopDiscovery() {
        activity.unregisterReceiver(receiver);
    }

    @Override
    public void createBluetoothServerConnection() {
        bluetoothServerConnection.start();
    }

    @Override
    public void closeBluetoothServerConnection() {
        bluetoothServerConnection.cancel();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    Log.e(TAG, "Here inside socket");
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's accept() method failed", e);
                        break;
                    }

                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
                if (connectedThread != null) connectedThread.cancel();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

//    private Handler handler; // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
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
//                    numBytes = mmInStream.read(mmBuffer);
                    byte[] targetArray = new byte[mmInStream.available()];
                    mmInStream.read(targetArray);
                    String readMessage = new String(targetArray);
                    if (!readMessage.isEmpty()) {
                        Log.d(TAG, "Read data: " + readMessage);

                        HashMap<String, String> params = new HashMap<>();
                        params.put("email", "default@mail.com");
                        params.put("type", "hit");
                        params.put("hit_type", "single");
                        params.put("description", "empty");
                        params.put("question", "Would you help this person?");
                        params.put("answer_choices", "y;n");
                        params.put("file", "single_choice.php");
                        params.put("lat", "59.34945964097764");
                        params.put("lng", "18.07227473706007");

                        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, CREATE_TASK_URL,
                                params, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if (status.equals("OK")) { // Everything's ok!
                                        Log.e(TAG, "Successfully create a task");
                                    } else { // something went wrong
                                        Log.e(TAG, response.getString("reason"));

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError response) {
                                Log.d(TAG + " ERR", response.toString());
                            }
                        });
                        queue.add(jsObjRequest);


                    }
                    // Send the obtained bytes to the UI activity.
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            mmBuffer);
//                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
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
