package com.omniwearhaptics.omniwearbtbridge;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;
import com.omniwearhaptics.omniwearbtbridge.logger.Log;

import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

/**
 * Implements simple BlueTooth communication to the OmniWear device.
 * Created by ehrenbrav on 11/23/16.
 */

class OmniWearBluetoothService {

    private static final String TAG = "OmniWearBluetoothService";

    private static final String PREFS_NAME = "OmniWearPrefs";
    private static final String BT_NAME = "OmniWear";
    private static final String OMNIWEAR_UUID = "99700001-ad20-11e6-8000-00805F9B34FB";
    private static final String OMNIWEAR_CHARACTERISTIC_UUID = "99700002-ad20-11e6-8000-00805F9B34FB";
    private static final String SAVED_MAC_PREF_NAME = "omniwear_device_mac";
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 3;

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;
    private static final int STATE_SEARCHING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mOmniWearDeviceService = null;
    private BluetoothGattCharacteristic mOmniWearDeviceCharacteristic = null;
    private static int mState = STATE_NONE;
    private Handler mHandler;

    // Constructor to check support for BT LE.
    OmniWearBluetoothService(Activity activity) {

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

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

        // Set up the handler.
        mHandler = new Handler();
    }

    // This should be called each time the app comes into focus.
    void resume(MainActivity activity) {

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mState == STATE_NONE) {

            // Check if there is a known OmniWear Device.
            SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
            String saved_mac = settings.getString(SAVED_MAC_PREF_NAME, "");

            // If so, try to connect with that device.
            if (!saved_mac.equals("")) {
                BluetoothDevice omniwearDevice = mBluetoothAdapter.getRemoteDevice(saved_mac);
                connect(omniwearDevice, activity);
            }
        }
    }

    // Cleanup.
    void stop(MainActivity activity) {

        Log.d(TAG, "stop");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mOmniWearDeviceService = null;
        mOmniWearDeviceCharacteristic = null;
        activity.setStatusMessage(activity.getString(R.string.status_not_connected));
        setState(STATE_NONE);
    }

    // Write the characteristic to the device to command a motor.
    void commandMotor(byte motor, byte intensity) {

        // Check if we're connected.
        if (mState != STATE_CONNECTED || mBluetoothGatt == null) {
            Log.i(TAG, "OmniWear device not connected.");
            return;
        }

        // Create the characteristic.
        /*
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(OMNIWEAR_CHARACTERISTIC_UUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
                */
        byte[] value = new byte[2];
        value[0] = motor;
        value[1] = intensity;
        mOmniWearDeviceCharacteristic.setValue(value);

        // Write the command to the device.
        Log.d(TAG, "Write");
        mBluetoothGatt.writeCharacteristic(mOmniWearDeviceCharacteristic);
    }

    // Serch for a new OmniWear device.
    void search(final MainActivity activity) {

        // Set up the BT LE scanner.
        final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        Toast.makeText(activity, "Starting scan...", Toast.LENGTH_LONG).show();

        // Callback function for the BT scanner.
        final ScanCallback btCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                super.onScanResult(callbackType, result);

                // If it's the OmniWear device, store the MAC and connect.
                BluetoothDevice device = result.getDevice();
                String msg = "Found device: " + device.getName();
                Log.d(TAG, msg);

                if (device.getName().equals(OmniWearBluetoothService.BT_NAME)) {

                    // Stop scanning.
                    scanner.stopScan(this);

                    // Save the address in memory.
                    String saved_mac = device.getAddress();

                    // Save the address in the preferences.
                    setSaved_mac(saved_mac, activity);

                    // Change the button text.
                    activity.setButtonText(activity.getString(R.string.forget_device));

                    // Try to connect.
                    connect(device, activity);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                String msg = "onScanFailed: " + errorCode;
                Log.d(TAG, msg);
                setState(STATE_NONE);
            }
        };

        // Start searching!
        scanner.startScan(btCallback);
        setState(STATE_SEARCHING);

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // Stop scan.
                scanner.stopScan(btCallback);

                // If we didn't find our device, notify user.
                if (mState == STATE_SEARCHING) {
                    activity.setStatusMessage(activity.getString(R.string.status_not_connected));
                    Toast.makeText(activity.getApplicationContext(),
                            activity.getText(R.string.device_not_found),
                            Toast.LENGTH_LONG
                    ).show();
                    setState(STATE_NONE);
                }
            }
        }, SCAN_PERIOD);
    }

    // Check if there's a saved MAC.
    static String getSaved_mac(Activity activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(SAVED_MAC_PREF_NAME, "");
    }

    // Set the MAC.
    static void setSaved_mac(String mac, Activity activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SAVED_MAC_PREF_NAME, mac);
        editor.apply();
    }

    // Set the connection state and log.
    private void setState(int state) {
        mState = state;
    }

    // Connect to the OmniWear device.
    private void connect(BluetoothDevice device, final MainActivity activity) {

        Log.d(TAG, "Attempting to connect to: " + device);

        // Indicate the status.
        activity.setStatusMessage(activity.getString(R.string.status_connecting));

        // Callbacks for interacting with the OmniWear Device.
        BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    // Connected - notify user and discover services.
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            activity.setStatusMessage(activity.getString(R.string.status_connected));
                        }
                    });
                    setState(STATE_CONNECTED);
                    Log.i(TAG, "Connected.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                } else if (newState == STATE_DISCONNECTED) {

                    // Disconnected - notify user.
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            activity.setStatusMessage(activity.getString(R.string.status_not_connected));
                        }
                    });
                    setState(STATE_NONE);
                    Log.i(TAG, "Not connected.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                // Find the service and characteristic for controlling the device.
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    mOmniWearDeviceService = gatt.getService(UUID.fromString(OMNIWEAR_UUID));
                    if (mOmniWearDeviceService == null) {
                        Log.w(TAG, "OmniWear service not found.");
                    } else {
                        Log.i(TAG, "OmniWear service discovered.");

                        // Get the characteristic for controlling the device.
                        mOmniWearDeviceCharacteristic = mOmniWearDeviceService.getCharacteristic(UUID.fromString(OMNIWEAR_CHARACTERISTIC_UUID));
                        if (mOmniWearDeviceCharacteristic == null) {
                            Log.w(TAG, "OmniWear characteristic not found.");
                        } else {
                            Log.i(TAG, "OmniWear Characteristic found.");
                        }
                    }
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Write successful.");
                }
            }
        };

        // Connect!
        mBluetoothGatt = device.connectGatt(activity.getApplicationContext(), true, mGattCallback);
        setState(STATE_CONNECTING);
    }
}
