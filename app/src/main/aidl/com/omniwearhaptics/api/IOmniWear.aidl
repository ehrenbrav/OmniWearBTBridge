/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 * Distributed under the Project Tango Preview Development Kit (PDK) Agreement.
 * CONFIDENTIAL. AUTHORIZED USE ONLY. DO NOT REDISTRIBUTE.
 */
package com.omniwearhaptics.api;

interface IOmniWear {
   void connectToDevice(String deviceMacAddress);
   int getConnectedDeviceType();
   void setMotor(int motorId, byte intensity);   
   void disconnect();
   void setDeviceType(int deviceType);
}
