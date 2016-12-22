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
import com.omniwearhaptics.api.OmniWearHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Android Service that other apps can use to interact with OmniWear devices.
 * 
 * @author Charles L. Chen (clc)
 */
public class OmniWearService extends Service {
    private static final String TAG = "OmniWearService";
    
    // TODO: Put in a proper permission check once the app is set to target 23 or higher. 
    // But since we are targeting 22, we can safely ignore this for the time being.

    private static OmniWearDriver mBTManager = null;
    private int mDeviceType = OmniWearHelper.DEVICETYPE_ERROR;

	@Override
	public void onCreate(){
		super.onCreate();
        mBTManager = new OmniWearDriver(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void onDestroy(){
		super.onDestroy();
		mBTManager.stop();
	}
	
	private byte getMotorByteFromId(int motorId){
        if (mDeviceType == OmniWearHelper.DEVICETYPE_CAP) {
            switch(motorId) {
                case OmniWearHelper.FRONT:
                    return 0x0;
                case OmniWearHelper.BACK:
                    return 0x1;
                case OmniWearHelper.RIGHT:
                    return 0x2;
                case OmniWearHelper.LEFT:
                    return 0x3;
                case OmniWearHelper.FRONT_RIGHT:
                    return 0x4;
                case OmniWearHelper.FRONT_LEFT:
                    return 0x5;
                case OmniWearHelper.BACK_RIGHT:
                    return 0x6;
                case OmniWearHelper.BACK_LEFT:
                    return 0x7;
                case OmniWearHelper.MID_FRONT:
                    return 0x8;
                case OmniWearHelper.MID_BACK:
                    return 0x9;
                case OmniWearHelper.MID_RIGHT:
                    return 0xa;
                case OmniWearHelper.MID_LEFT:
                    return 0xb;
                case OmniWearHelper.TOP:
                    return 0xc;
                default:
                    Log.e(TAG, "Invalid motor_location in motor function");
                    return 0x0;
            }
        } else if (mDeviceType == OmniWearHelper.DEVICETYPE_NECKBAND) {
            return (byte) motorId;
        }

        // Error.
        Log.e(TAG, "Invalid device type in motor function");
        return 0;
	}
	
	private IOmniWear.Stub mBinder = new  IOmniWear.Stub(){
		@Override
		public void connectToDevice(String deviceMacAddress)
				throws RemoteException {
			if (deviceMacAddress.length() < 1){
				mBTManager.search(OmniWearService.this);
			} else {
				Log.e(TAG, "connectToDevice with a MAC address is not implemented yet. :(");
			}
		}

		// This is weird - why can't the Omniwear self-identify without the user needing to set the type?
		@Override
		public void setDeviceType(int deviceType){
			mDeviceType = deviceType;
		}

		@Override
		public int getConnectedDeviceType() throws RemoteException {
			if (mBTManager.getState() != OmniWearDriver.STATE_CONNECTED) {
				return OmniWearHelper.DEVICETYPE_ERROR;
			}
			return mDeviceType;
		}

		@Override
		public void setMotor(int motorId, byte intensity)
				throws RemoteException {
			if (getConnectedDeviceType() == OmniWearHelper.DEVICETYPE_ERROR) {
				return;
			}			
			mBTManager.commandMotor(getMotorByteFromId(motorId), intensity);
		}

		@Override
		public void disconnect() throws RemoteException {
			mBTManager.stop();
		}
	};
	
	
	


}
