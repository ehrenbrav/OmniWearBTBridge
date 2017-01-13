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

package com.omniwearhaptics.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;


/**
 * Public facing API for interacting with OmniWear devices.
 * 
 * @author Charles L. Chen (clc) and Ehren J. Brav (ehrenbrav)
 */
public class OmniWearHelper {

    // Constants for the device type.
	public static final byte DEVICETYPE_ERROR = 0x0;
	public static final byte DEVICETYPE_CAP = 0x1;
	public static final byte DEVICETYPE_NECKBAND = 0x2;
	public static final byte DEVICETYPE_WRISTBAND = 0x3;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;
    public static final int STATE_SEARCHING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    // Motor IDs.
    public static final byte FRONT =        0x0;
    public static final byte BACK =         0x1;
    public static final byte RIGHT =        0x2;
    public static final byte LEFT =         0x3;
    public static final byte FRONT_RIGHT =  0x4;
    public static final byte FRONT_LEFT =   0x5;
    public static final byte BACK_RIGHT =   0x6;
    public static final byte BACK_LEFT =    0x7;
    public static final byte MID_FRONT =    0x8;
    public static final byte MID_BACK =     0x9;
    public static final byte MID_RIGHT =    0xa;
    public static final byte MID_LEFT =     0xb;
    public static final byte TOP =          0xc;

    // Motor off constant.
    public static final byte OFF =          0x0;

    // Private fields relating to the OmniWear service.
	private ServiceConnection mServiceConnection;
	private Context mParent;
	private IOmniWear mOmniWearInterface;

    // Callback functions that the client app implements.
    private OnStateChangeListener mOnStateChangeListener;
    public interface OnStateChangeListener {
        void OnStateChange(int newState);
    }
    private IOmniWearCallback mCallback = new IOmniWearCallback.Stub() {

        public void onStateChange(int newState) throws RemoteException {
            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.OnStateChange(newState);
            }
        }
    };

    // Start everything. If deviceMAC is empty, search for any OmniWear device.
	public OmniWearHelper(final Context context, OnStateChangeListener listener) {

		mParent = context;
        mOnStateChangeListener = listener;
		Intent intent = new Intent();
		intent.setClassName("com.omniwearhaptics.omniwearbtbridge",
				"com.omniwearhaptics.omniwearbtbridge.OmniWearService");

        // Connect to the OmniWear service.
		mServiceConnection = new ServiceConnection() {

            // When the service is bound, try to connect to the device.
			public void onServiceConnected(ComponentName name, IBinder service) {

                // Create the interface.
				mOmniWearInterface = IOmniWear.Stub.asInterface(service);
			}

			public void onServiceDisconnected(ComponentName name) {
                try {
                    mOmniWearInterface.unregisterCallback();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
			}
		};
		context.bindService(intent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	public void shutdown() {

        disconnect();
		if (mServiceConnection != null) {
			mOmniWearInterface = null;
			mParent.unbindService(mServiceConnection);
			mServiceConnection = null;
            mOnStateChangeListener = null;
		}
	}

    public void disconnect() {

        if (mOmniWearInterface != null) {
            try {
                mOmniWearInterface.disconnect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Search for a new OmniWear device.
    public void searchForOmniWearDevice() {

        if (mOmniWearInterface != null) {
            try {
                mOmniWearInterface.searchForOmniWearDevice();
                mOmniWearInterface.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Connect to a known OmniWear device.
    public void connectToKnownDevice(String deviceMAC) {

        if (mOmniWearInterface != null) {
            try {
                mOmniWearInterface.connectToKnownDevice(deviceMAC);
                mOmniWearInterface.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

	public void setMotor(byte motorId, byte intensity) {

		if (mOmniWearInterface != null){
			try {
				mOmniWearInterface.setMotor(motorId, intensity);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}		
	}

    // Read the device type using BT.
    public int getConnectedDeviceType() {

        if (mOmniWearInterface != null) {
            try {
                return mOmniWearInterface.getConnectedDeviceType();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return DEVICETYPE_ERROR;
    }

    // Return the MAC of the connected device.
    public String getConnectedDeviceMAC() {

        if (mOmniWearInterface != null) {
            try {
                return mOmniWearInterface.getConnectedDeviceMAC();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    // Get the connection state.
    public int getState() {

        if (mOmniWearInterface != null) {
            try {
                return mOmniWearInterface.getState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return STATE_NONE;
    }
}