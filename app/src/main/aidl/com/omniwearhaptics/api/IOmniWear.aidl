/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 * Distributed under the Project Tango Preview Development Kit (PDK) Agreement.
 * CONFIDENTIAL. AUTHORIZED USE ONLY. DO NOT REDISTRIBUTE.
 */
package com.omniwearhaptics.api;

import com.omniwearhaptics.api.IOmniWearCallback;

interface IOmniWear {
   void registerCallback(IOmniWearCallback callback);
   void unregisterCallback();
   void searchForOmniWearDevice();
   void connectToKnownDevice(String deviceMacAddress);
   int getConnectedDeviceType();
   String getConnectedDeviceMAC();
   void setMotor(byte motorId, byte intensity);
   int getState();
   void disconnect();
}
