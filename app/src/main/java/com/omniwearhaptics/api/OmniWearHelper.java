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
 * @author Charles L. Chen (clc)
 */
public class OmniWearHelper {
	public static final int DEVICETYPE_ERROR = -1;
	public static final int DEVICETYPE_CAP = 0;
	public static final int DEVICETYPE_NECKBAND = 1;
	
	
    public static final int FRONT =        0;
    public static final int BACK =         1;
    public static final int RIGHT =        2;
    public static final int LEFT =         3;
    public static final int FRONT_RIGHT =  4;
    public static final int FRONT_LEFT =   5;
    public static final int BACK_RIGHT =   6;
    public static final int BACK_LEFT =    7;
    public static final int MID_FRONT =    8;
    public static final int MID_RIGHT =    9;
    public static final int MID_BACK =     10;
    public static final int MID_LEFT =     11;
    public static final int TOP =          12;

    public static final byte OFF =          0;
    public static final byte ON =           100;

    
	private ServiceConnection mServiceConnection;
	private Context mParent;
	private IOmniWear mOmniWear;
		
	public OmniWearHelper(final Context ctx, final int deviceType, final Runnable onConnected) {
		mParent = ctx;
		Intent intent = new Intent();
		intent.setClassName("com.omniwearhaptics.omniwearbtbridge",
				"com.omniwearhaptics.omniwearbtbridge.OmniWearService");
		mServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				mOmniWear = IOmniWear.Stub.asInterface(service);
				try {
					mOmniWear.setDeviceType(deviceType);
					mOmniWear.connectToDevice("");
					if (onConnected != null){
						waitForConnection(onConnected);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			public void onServiceDisconnected(ComponentName name) {
			}
		};
		ctx.bindService(intent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	private void waitForConnection(final Runnable onConnected){
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					if (mOmniWear == null){
						return;
					}
					if (mOmniWear.getConnectedDeviceType() != DEVICETYPE_ERROR) {
						new Thread(onConnected).start();
					} else {
						waitForConnection(onConnected);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}).start();
	}
	
	public void shutdown(){
		try {
			mOmniWear.disconnect();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (mServiceConnection != null) {
			mOmniWear = null;
			mParent.unbindService(mServiceConnection);
			mServiceConnection = null;
		}
	}
	
	public void setMotor(int motorId, byte intensity){
		if (mOmniWear != null){
			try {
				mOmniWear.setMotor(motorId, intensity);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}		
	}
	
}
