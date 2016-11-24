package com.omniwearhaptics.omniwearbtbridge;

/**
 * Defines several constants used between {@link OmniWearBluetoothService} and the UI.
 */
interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int DEVICE_FOUND = 2;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String TOAST = "toast";
}