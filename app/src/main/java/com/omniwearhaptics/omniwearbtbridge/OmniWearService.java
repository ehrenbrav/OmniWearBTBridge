/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.omniwearhaptics.omniwearbtbridge;

import com.omniwearhaptics.api.IOmniWear;
import com.omniwearhaptics.api.IOmniWearCallback;
import com.omniwearhaptics.api.OmniWearHelper;

import android.app.Service;
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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

/**
 * Android Service that other apps can use to interact with OmniWear devices.
 * 
 * @author Charles L. Chen (clc) and Ehren J. Brav (ehrenbrav)
 */
public class OmniWearService extends Service {

    private static final String TAG = "OmniWearService";
    
    // TODO: Put in a proper permission check once the app is set to target 23 or higher. 
    // But since we are targeting 22, we can safely ignore this for the time being.

    // Constants from the firmware.
    private static final String BT_NAME = "OmniWear";
    private static final String OMNIWEAR_UUID = "99700001-ad20-11e6-8000-00805F9B34FB";
    private static final String OMNIWEAR_CHARACTERISTIC_UUID = "99700002-ad20-11e6-8000-00805F9B34FB";
    private static final String OMNIWEAR_DEVICE_TYPE_CHARACTERISTIC_UUID = "99700003-ad20-11e6-8000-00805F9B34FB";

    // Duration of scan.
    private static final long SCAN_PERIOD = 10000;

    // State and BlueTooth fields.
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mOmniWearDeviceService = null;
    private BluetoothGattCharacteristic mOmniWearDeviceCharacteristic = null;
    private BluetoothGattCharacteristic mOmniWearDeviceTypeCharacteristic = null;
    private Handler mHandler;
    private IOmniWearCallback mCallback;
    private static String mConnectedDeviceMAC = "";
    private static byte mDeviceType = OmniWearHelper.DEVICETYPE_ERROR;
    private static int mState = OmniWearHelper.EVENT_STATE_NONE;

    @Override
	public void onCreate(){
		super.onCreate();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not available");
            Toast.makeText(OmniWearService.this, "Bluetooth is not available - cannot continue", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE not supported");
            Toast.makeText(OmniWearService.this, "Bluetooth Low Energy is not supported - cannot continue", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        // Ensures Bluetooth is on.
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "BlueTooth is not enabled.");
            stopSelf();
            return;
        }

        // Set up the handler.
        mHandler = new Handler();
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        stop();
    }

	@Override
	public IBinder onBind(Intent intent) {
        return mBinder;
	}

    // Implementation of OmniWear API.
	private IOmniWear.Stub mBinder = new IOmniWear.Stub() {

        // Register a callback to communicate with the client app.
        @Override
        public void registerCallback(IOmniWearCallback callback) throws RemoteException {
            mCallback = callback;
        }

        // Unregister a callback to communicate with the client app.
        @Override
        public void unregisterCallback() throws RemoteException {
            mCallback = null;
        }

        // Search for a new OmniWear device.
        @Override
        public void searchForOmniWearDevice() throws RemoteException {

            // Error check.
            if (mState != OmniWearHelper.EVENT_STATE_NONE) {
                Log.e(TAG, "searchForOmniWearDevice: state is not EVENT_STATE_NONE");
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Log.e(TAG, "BlueTooth is not enabled");
                return;
            }

            // Set up the BT LE scanner.
            final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            // Callback function for the BT scanner.
            final ScanCallback btCallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    super.onScanResult(callbackType, result);

                    // Examine this device...
                    BluetoothDevice device = result.getDevice();
                    String deviceName = device.getName();

                    // Throw out empty or null names.
                    if (deviceName == null || deviceName.isEmpty() ) { return; }

                    // Log for debugging.
                    Log.d(TAG, "Found device: " + device.getName());

                    // Check if it's an OmniWear device.
                    if (device.getName().equals(BT_NAME)) {

                        // Stop scanning.
                        scanner.stopScan(this);

                        // Try to connect.
                        try {
                            mCallback.onOmniWearEvent(OmniWearHelper.EVENT_DEVICE_FOUND);
                            connectToKnownDevice(device.getAddress());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.w(TAG, "onScanFailed: " + errorCode);
                    setState(OmniWearHelper.EVENT_STATE_NONE);
                }
            };

            // Start searching!
            scanner.startScan(btCallback);
            setState(OmniWearHelper.EVENT_STATE_SEARCHING);

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    // Stop scan.
                    scanner.stopScan(btCallback);

                    // Didn't find our device.
                    if (mState == OmniWearHelper.EVENT_STATE_SEARCHING) {
                        Log.i(TAG, "Device not found");
                        setState(OmniWearHelper.EVENT_STATE_NONE);
                        try {
                            mCallback.onOmniWearEvent(OmniWearHelper.EVENT_DEVICE_NOT_FOUND);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, SCAN_PERIOD);
        }

		@Override
		public void connectToKnownDevice(String deviceMacAddress) throws RemoteException {

            // Error check.
            if (deviceMacAddress == null) {
                Log.e(TAG, "deviceMacAddress is null in connectToDevice");
                return;
            }
            if (mState == OmniWearHelper.EVENT_STATE_CONNECTED || mState == OmniWearHelper.EVENT_STATE_CONNECTING) {
                Log.e(TAG, "connectToKnownDevice: state is connected or connecting.");
                return;
            }

            // Create the device using the MAC.
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceMacAddress);
            Log.d(TAG, "Attempting to connect to: " + device);

            // Callbacks for interacting with the OmniWear Device.
            BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        setState(OmniWearHelper.EVENT_STATE_CONNECTED);
                        Log.i(TAG, "Connected.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == STATE_DISCONNECTED) {

                        setState(OmniWearHelper.EVENT_STATE_NONE);
                        Log.i(TAG, "Not connected.");
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    // Find the service and characteristic for controlling the device.
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mOmniWearDeviceService = gatt.getService(UUID.fromString(OMNIWEAR_UUID));
                        if (mOmniWearDeviceService == null) {

                            // Weird - no OmniWear service...cancel.
                            Log.w(TAG, "OmniWear service not found.");
                            stop();
                        } else {
                            Log.i(TAG, "OmniWear service discovered.");

                            // Get the characteristic for controlling the device.
                            mOmniWearDeviceCharacteristic = mOmniWearDeviceService.getCharacteristic(UUID.fromString(OMNIWEAR_CHARACTERISTIC_UUID));
                            if (mOmniWearDeviceCharacteristic == null) {

                                // Weird - no OmniWear chacteristic...cancel.
                                Log.w(TAG, "OmniWear characteristic not found.");
                                stop();
                                return;
                            } else {
                                // Success.
                                Log.i(TAG, "OmniWear Characteristic found.");
                            }

                            // Get the device type characteristic.
                            mOmniWearDeviceTypeCharacteristic = mOmniWearDeviceService.getCharacteristic(UUID.fromString(OMNIWEAR_DEVICE_TYPE_CHARACTERISTIC_UUID));
                            if (mOmniWearDeviceTypeCharacteristic == null) {

                                // Weird - no device type characteristic...cancel.
                                Log.w(TAG, "Device type characteristic not found.");
                                stop();
                                return;
                            } else {
                                // Success.
                                Log.i(TAG, "Device type Characteristic found.");
                            }

                            // Save the MAC.
                            mConnectedDeviceMAC = gatt.getDevice().getAddress();

                            // Save the device type.
                            mBluetoothGatt.readCharacteristic(mOmniWearDeviceTypeCharacteristic);
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

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Read successful");
                    } else {
                        Log.e(TAG, "Read failed");
                    }

                    // Handle the device type characteristic.
                    if (characteristic == mOmniWearDeviceTypeCharacteristic) {

                        byte[] deviceType = characteristic.getValue();

                        // Error check.
                        if (deviceType[0] != OmniWearHelper.DEVICETYPE_CAP &&
                                deviceType[0] != OmniWearHelper.DEVICETYPE_NECKBAND &&
                                deviceType[0] != OmniWearHelper.DEVICETYPE_WRISTBAND) {
                            Log.e(TAG, "onServicesDiscovered: invalid device type returned from device");
                            stop();
                            return;
                        }
                        mDeviceType = deviceType[0];
                        Log.i(TAG, "Device Type is " + mDeviceType);
                    }
                }
            };

            // Connect!
            mBluetoothGatt = device.connectGatt(OmniWearService.this, true, mGattCallback);
            setState(OmniWearHelper.EVENT_STATE_CONNECTING);
        }

		@Override
		public int getConnectedDeviceType() throws RemoteException {

            // Error check.
            if (mState != OmniWearHelper.EVENT_STATE_CONNECTED) {
                Log.w(TAG, "getDeviceType(): no connected device");
                return OmniWearHelper.DEVICETYPE_ERROR;
            }
            return mDeviceType;
        }

        @Override
        public String getConnectedDeviceMAC() throws RemoteException {
            if (mState != OmniWearHelper.EVENT_STATE_CONNECTED) {
                Log.w(TAG, "getConnectedDeviceMAC: no current connected device");
                return "";
            }
            return mConnectedDeviceMAC;
        }

		@Override
		public void setMotor(byte motorId, byte intensity) throws RemoteException {

			if (mDeviceType == OmniWearHelper.DEVICETYPE_ERROR) {
                Log.w(TAG, "setMotor: DeviceType is unkown.");
                return;
			}

            // Check if we're connected.
            if (mState != OmniWearHelper.EVENT_STATE_CONNECTED || mBluetoothGatt == null) {
                Log.w(TAG, "setMotor: OmniWear device not connected.");
                return;
            }

            // Set up the value.
            byte[] value = new byte[2];
            value[0] = motorId;
            value[1] = intensity;
            mOmniWearDeviceCharacteristic.setValue(value);

            // Write the command to the device.
            Log.d(TAG, "Write: " + value[0] + " " + value[1]);
            mBluetoothGatt.writeCharacteristic(mOmniWearDeviceCharacteristic);
        }

		@Override
		public void disconnect() throws RemoteException {
			stop();
		}

        @Override
        public int getState() throws RemoteException {
            return mState;
        }
	};

    // Set the state and fire a callback.
    private void setState(int newState) {
        mState = newState;
        if (mCallback != null) {
            try {
                mCallback.onOmniWearEvent(newState);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "mCallback is null in setState");
        }
    }

    // Cleanup.
    private void stop() {
        Log.d(TAG, "stop");

        if (mBluetoothGatt == null) {
            return;
        }
        mDeviceType = OmniWearHelper.DEVICETYPE_ERROR;
        mConnectedDeviceMAC = "";
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mOmniWearDeviceService = null;
        mOmniWearDeviceCharacteristic = null;
        setState(OmniWearHelper.EVENT_STATE_NONE);
    }
}
