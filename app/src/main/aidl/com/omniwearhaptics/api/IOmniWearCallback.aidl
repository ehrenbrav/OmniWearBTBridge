// IOmniWearCallback.aidl
package com.omniwearhaptics.api;

// Declare any non-default types here with import statements

interface IOmniWearCallback {

    void onOmniWearEvent(int newState);
    void onOmniWearLog(int priority, String tag, String msg);
}
