package com.omniwearhaptics.omniwearbtbridge;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * The Handler that gets information back from OmniWearBluetoothService.
 */

final class OmniWearHandler extends Handler {

    private final MainActivity mActivity;

    OmniWearHandler(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case Constants.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case OmniWearBluetoothService.STATE_CONNECTED:
                        mActivity.setStatusMessage(mActivity.getString(R.string.status_connected));
                        break;
                    case OmniWearBluetoothService.STATE_CONNECTING:
                        mActivity.setStatusMessage(mActivity.getString(R.string.status_connecting));
                        break;
                    case OmniWearBluetoothService.STATE_SEARCHING:
                        mActivity.setStatusMessage(mActivity.getString(R.string.status_searching));
                        break;
                    case OmniWearBluetoothService.STATE_NONE:
                        mActivity.setStatusMessage(mActivity.getString(R.string.status_not_connected));
                        break;
                }
                break;
            case Constants.DEVICE_FOUND:
                String output = mActivity.getString(R.string.device_found) + msg.arg1;
                mActivity.setStatusMessage(output);
                break;
            case Constants.MESSAGE_TOAST:
                Toast.makeText(mActivity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
