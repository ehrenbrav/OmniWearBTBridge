package com.omniwearhaptics.omniwearbtbridge;

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

public class OmniWearDriver {

    private static final String TAG = "OmniWearDriver";

    private static final String PREFS_NAME = "OmniWearPrefs";
    private static final String BT_NAME = "OmniWear";
    private static final String OMNIWEAR_UUID = "99700001-ad20-11e6-8000-00805F9B34FB";
    private static final String OMNIWEAR_CHARACTERISTIC_UUID = "99700002-ad20-11e6-8000-00805F9B34FB";
    private static final String SAVED_MAC_PREF_NAME = "omniwear_device_mac";
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 3;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;
    public static final int STATE_SEARCHING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mOmniWearDeviceService = null;
    private BluetoothGattCharacteristic mOmniWearDeviceCharacteristic = null;
    private static int mState = STATE_NONE;
    private Handler mHandler;

    // Constructor to check support for BT LE.
    public OmniWearDriver(Context activity) {

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not available");
            return;
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE not supported");
            return;
        }

        // Set up the handler.
        mHandler = new Handler();
    }


    // Cleanup.
    public void stop() {
        Log.d(TAG, "stop");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mOmniWearDeviceService = null;
        mOmniWearDeviceCharacteristic = null;
        setState(STATE_NONE);
    }

    // Write the characteristic to the device to command a motor.
    public void commandMotor(byte motor, byte intensity) {

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
        Log.d(TAG, "Write: " + value[0] + " " + value[1]);
        mBluetoothGatt.writeCharacteristic(mOmniWearDeviceCharacteristic);
    }

    // Serch for a new OmniWear device.
    public void search(final Context activity) {

        // Set up the BT LE scanner.
        final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Callback function for the BT scanner.
        final ScanCallback btCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                super.onScanResult(callbackType, result);

                // If it's the OmniWear device, store the MAC and connect.
                BluetoothDevice device = result.getDevice();
                String msg = "Found device: " + device.getName();
                Log.d(TAG, msg);

                String deviceName = device.getName();
                if (deviceName == null || deviceName.isEmpty() ) { return; }

                if (device.getName().equals(OmniWearDriver.BT_NAME)) {

                    // Stop scanning.
                    scanner.stopScan(this);

                    // Save the address in memory.
                    String saved_mac = device.getAddress();

                    // Save the address in the preferences.
                    setSaved_mac(saved_mac, activity);

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
                    Log.e(TAG, "Device not found");
                    setState(STATE_NONE);
                }
            }
        }, SCAN_PERIOD);
    }

    // Check if there's a saved MAC.
    public static String getSaved_mac(Context activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(SAVED_MAC_PREF_NAME, "");
    }

    // Set the MAC.
    public static void setSaved_mac(String mac, Context activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SAVED_MAC_PREF_NAME, mac);
        editor.apply();
    }

    // Set the connection state and log.
    private void setState(int state) {
        mState = state;
    }

    public int getState(){
    	return mState;
    }
    
    // Connect to the OmniWear device.
    public void connect(BluetoothDevice device, final Context activity) {

        Log.d(TAG, "Attempting to connect to: " + device);

        // Callbacks for interacting with the OmniWear Device.
        BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setState(STATE_CONNECTED);
                    Log.i(TAG, "Connected.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                } else if (newState == STATE_DISCONNECTED) {

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

                        // Weird - forget the device.
                        Log.w(TAG, "OmniWear service not found.");
                        OmniWearDriver.setSaved_mac("", activity);
                        stop();
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
