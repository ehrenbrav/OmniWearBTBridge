package com.omniwearhaptics.omniwearbtbridge;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.omniwearhaptics.omniwearbtbridge.logger.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Implements simple BlueTooth communication to the OmniWear device.
 * Created by ehrenbrav on 11/23/16.
 */

class OmniWearBluetoothService {

    private static final String PREFS_NAME = "OmniWearPrefs";
    private static final String OMNIWEAR_UUID = "99700001-ad20-11e6-8000-00805F9B34FB";
    private static final String TAG = "OmniWearBluetoothService";
    private static final String SAVED_MAC_PREF_NAME = "omniwear_device_mac";
    private static final int REQUEST_ENABLE_BT = 3;

    // Constants that indicate the current connection state
    static final int STATE_NONE = 0;
    static final int STATE_SEARCHING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;

    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private static String saved_mac = "";
    private static boolean mIsReceiverRegistered = false;
    private static int mState = STATE_NONE;
    private ConnectedThread mConnectedThread = null;
    private ConnectThread mConnectThread = null;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Log it.
                mHandler.obtainMessage(Constants.DEVICE_FOUND, device.getName()).sendToTarget();

                // See if it's the OmniWear device.
                if (device.getName().equals(context.getResources().getText(R.string.omniwear_device_name))) {

                    // Record the device's MAC.
                    saved_mac = device.getAddress();
                    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(SAVED_MAC_PREF_NAME, saved_mac);

                    // Commit the edits!
                    editor.apply();

                    // Try to connect.
                    BluetoothDevice omniwearDevice = mBluetoothAdapter.getRemoteDevice(saved_mac);
                    connect(omniwearDevice);
                }
            }

            // Notify we're starting discovery.
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(context, "BlueTooth discovery started...", Toast.LENGTH_LONG).show();
            }
        }
    };

    OmniWearBluetoothService(Activity activity, Handler handler) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity.getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
        }

    }

    // This should be called each time the app comes into focus.
    synchronized void resume(Activity activity) {

        // See if BT is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (mState == STATE_NONE) {

            // Check if there is a known OmniWear Device.
            SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
            saved_mac = settings.getString(SAVED_MAC_PREF_NAME, "");

            // If so, try to connect with that device.
            if (!saved_mac.equals("")) {
                BluetoothDevice omniwearDevice = mBluetoothAdapter.getRemoteDevice(saved_mac);
                connect(omniwearDevice);
            }
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    // Cleanup.
    synchronized void stop(Context context) {

        Log.d(TAG, "stop");

        // Unregister broadcast receiver if necessary.
        if (!mIsReceiverRegistered) {
            context.unregisterReceiver(mReceiver);
            mIsReceiverRegistered = false;
        }

        // Stop all threads.
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        setState(STATE_NONE);
    }

    // Serch for a new OmniWear device.
    void search(Context context) {

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mIsReceiverRegistered = true;

        // Start searching!
        if (!mBluetoothAdapter.startDiscovery()) {
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "BlueTooth discovery failed.");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        setState(STATE_SEARCHING);
    }

    // Check if there's a saved MAC.
    static String getSaved_mac() {
        return saved_mac;
    }

    // Set the MAC.
    static void setSaved_mac(String mac) {
        saved_mac = mac;
    }

    // Set the connection state.
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    private synchronized void connect(BluetoothDevice device) {

        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    private synchronized void connected(BluetoothSocket socket) {

        Log.d(TAG, "Connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        ConnectThread(BluetoothDevice device) {

            Log.d(TAG, "Connect to: " + device);

            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(OMNIWEAR_UUID));
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread create socket failed.", e);
            }
            mmSocket = tmp;
        }

        public void run() {

            Log.i(TAG, "BEGIN mConnectThread");

            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(TAG, "ConnectThread connect() failed.", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "ConnectThread close socket failed.", closeException);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (OmniWearBluetoothService.this) {
                mConnectThread = null;
            }

            // OK here we go...
            connected(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        void cancel() {
            try {
                mmSocket.close();
                mConnectedThread = null;
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {

            Log.d(TAG, "Created ConnectedThread");
            mmSocket = socket;
            OutputStream tmpOut = null;

            // Get the output stream, using temp objects because
            // member streams are final
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread - could not get output stream", e);
            }

            mmOutStream = tmpOut;
        }

        /* Call this from the main activity to send data to the remote device */
        void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread - write(bytes) failed", e);
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        void cancel() {
            try {
                mmSocket.close();
                mConnectedThread = null;
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
        }
    }
}
