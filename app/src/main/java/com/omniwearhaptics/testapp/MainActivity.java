package com.omniwearhaptics.testapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.omniwearhaptics.api.OmniWearHelper;
import com.omniwearhaptics.omniwearbtbridge.R;
import com.omniwearhaptics.omniwearbtbridge.common.ActivityBase;
import com.omniwearhaptics.omniwearbtbridge.logger.Log;
import com.omniwearhaptics.omniwearbtbridge.logger.LogFragment;
import com.omniwearhaptics.omniwearbtbridge.logger.LogWrapper;
import com.omniwearhaptics.omniwearbtbridge.logger.MessageOnlyLogFilter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends ActivityBase {

    public static final String TAG = "MainActivity";

    private static boolean mLogShown = false;
    private static OmniWearHelper mHelper;
    private static byte mIntensity = 100;
    private static final String PREFS_NAME = "OmniWearPrefs";
    private static final String SAVED_MAC_PREF_NAME = "omniwear_device_mac";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the view.
        setContentView(R.layout.activity_main);

        // Set up the intensity slider.
        SeekBar intensitySlider = (SeekBar) findViewById(R.id.intensitySlider);
        intensitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int intensity, boolean b) {
                setIntensity(intensity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing.
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set default options.
        setStatusMessage(getString(R.string.status_not_connected));
        Button logButton = (Button) findViewById(R.id.button_toggle_log);
        logButton.setText(R.string.show_log);
        ViewAnimator logFragment = (ViewAnimator) findViewById(R.id.log_fragment_animator);
        logFragment.setVisibility(View.INVISIBLE);

        // Hide buttons.
        hideButtons();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled. If so, enable Helper.
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable BlueTooth", Toast.LENGTH_LONG).show();
            finish();
        } else {

            // Create the OmniWear helper.
            mHelper = new OmniWearHelper(this, new OmniWearHelper.OnOmniWearEventListener() {

                @Override
                public void OnOmniWearEvent(final int event) {

                    // So our callback can tinker with the app's UI.
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            switch (event) {

                                case OmniWearHelper.EVENT_STATE_CONNECTING:
                                    setStatusMessage(getString(R.string.status_connecting));
                                    break;
                                case OmniWearHelper.EVENT_STATE_CONNECTED:
                                    configButtonUI();
                                    configButtonVisibility();
                                    setSaved_mac(mHelper.getConnectedDeviceMAC());
                                    setButtonText(getString(R.string.forget_device));
                                    setStatusMessage(getString(R.string.status_connected));
                                    Toast.makeText(MainActivity.this, "Connected to OmniWear Device", Toast.LENGTH_SHORT).show();
                                    break;
                                case OmniWearHelper.EVENT_STATE_SEARCHING:
                                    setStatusMessage(getString(R.string.status_searching));
                                    break;
                                case OmniWearHelper.EVENT_STATE_NONE:
                                    setStatusMessage(getString(R.string.status_not_connected));
                                    hideButtons();
                                    break;
                                case OmniWearHelper.EVENT_DEVICE_FOUND:
                                    Toast.makeText(MainActivity.this, "OmniWear Device Found", Toast.LENGTH_LONG).show();
                                    break;
                                case OmniWearHelper.EVENT_DEVICE_NOT_FOUND:
                                    Toast.makeText(MainActivity.this, "OmniWear Device Not Found", Toast.LENGTH_LONG).show();
                                    break;
                                case OmniWearHelper.EVENT_SERVICE_BOUND:
                                    // If there's a saved MAC, try to connect.
                                    String savedMAC = getSaved_mac();
                                    if (savedMAC.equals("")) {
                                        setButtonText(getString(R.string.pair_device));
                                    } else {
                                        setButtonText(getString(R.string.forget_device));
                                        mHelper.connectToKnownDevice(savedMAC);
                                    }
                                    break;
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHelper != null) {mHelper.shutdown();}
        // Force quitting the app since we should actually be disabling all the buttons here (but we aren't).
        // If we didn't force quit here, we'd have a crash later on if we tried to use the buttons before pairing.
        finish();
    }

    // Handle forgetting and pairing of the device.
    public void onPairingButtonClicked(View view) {

        Button pairingButton = (Button) findViewById(R.id.button_pairing);

        // Handle pairing
        if (pairingButton.getText().equals(getString(R.string.pair_device))) {

            // Search for a new OmniWear device.
            mHelper.searchForOmniWearDevice();
        } else if (pairingButton.getText().equals(getString(R.string.forget_device))) {

            // Delete the saved MAC.
            setSaved_mac("");

            // Allow the user to search for a new device.
            setButtonText(getString(R.string.pair_device));

            // Disconnect from any existing device.
            mHelper.disconnect();

        } else {
            Log.e(TAG, "paringButton text is in unknown state.");
        }
    }

    // Show or toggle the log.
    public void onToggleLogButtonClicked(View view) {

        Button button = (Button) findViewById(R.id.button_toggle_log);
        ViewAnimator logFragment = (ViewAnimator) findViewById(R.id.log_fragment_animator);
        if (mLogShown) {
            button.setText(R.string.show_log);
            logFragment.setVisibility(View.INVISIBLE);
            mLogShown = false;
        } else {
            logFragment.setVisibility(View.VISIBLE);
            button.setText(R.string.hide_log);
            mLogShown = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.log_fragment);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    // Set the intensity variable.
    private static void setIntensity(int intensity) {
        mIntensity = (byte) intensity;
        Log.i(TAG, "Intensity set to: " + intensity);
    }

    // Configure the behavior of the buttons.
    private void configButtonUI() {

        // Error check.
        if (mHelper == null) {
            Log.e(TAG, "mHelper null in configButtonUI");
            return;
        }

        // Set up touch listeners on the motor buttons.
        findViewById(R.id.button_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.FRONT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.FRONT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_front_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.FRONT_RIGHT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.FRONT_RIGHT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.RIGHT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.RIGHT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_back_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.BACK_RIGHT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.BACK_RIGHT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.BACK), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.BACK), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_back_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.BACK_LEFT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.BACK_LEFT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.LEFT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.LEFT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_front_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.FRONT_LEFT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.FRONT_LEFT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_middle_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.MID_FRONT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.MID_FRONT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_middle_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.MID_RIGHT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.MID_RIGHT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_middle_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.MID_BACK), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.MID_BACK), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_middle_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor((OmniWearHelper.MID_LEFT), OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor((OmniWearHelper.MID_LEFT), mIntensity); }
                return false;
            }
        });
        findViewById(R.id.button_top).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { mHelper.setMotor(OmniWearHelper.TOP, OmniWearHelper.OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { mHelper.setMotor(OmniWearHelper.TOP, mIntensity); }
                return false;
            }
        });
    }

    // Hides or shows the buttons that differ between device types.
    private void configButtonVisibility() {

        // Error check.
        if (mHelper == null) {
            Log.e(TAG, "mHelper null in configButtonVisibility");
            return;
        }

        // Query the device for how many motors it has.
        int deviceType = mHelper.getConnectedDeviceType();

        switch(deviceType) {
            case OmniWearHelper.DEVICETYPE_CAP:
                // Show cap-only buttons.
                findViewById(R.id.button_middle_back).setVisibility(View.VISIBLE);
                findViewById(R.id.button_middle_front).setVisibility(View.VISIBLE);
                findViewById(R.id.button_middle_left).setVisibility(View.VISIBLE);
                findViewById(R.id.button_middle_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_top).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back_left).setVisibility(View.VISIBLE);
                findViewById(R.id.button_left).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front_left).setVisibility(View.VISIBLE);
                break;
            case OmniWearHelper.DEVICETYPE_NECKBAND:
                // Hide cap-only buttons.
                findViewById(R.id.button_middle_back).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_front).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_left).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_right).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_top).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_front).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back_right).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back).setVisibility(View.VISIBLE);
                findViewById(R.id.button_back_left).setVisibility(View.VISIBLE);
                findViewById(R.id.button_left).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front_left).setVisibility(View.VISIBLE);
                break;
            case OmniWearHelper.DEVICETYPE_WRISTBAND:
                // Show only the single button.
                findViewById(R.id.button_middle_back).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_front).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_left).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_middle_right).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_top).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_front).setVisibility(View.VISIBLE);
                findViewById(R.id.button_front_right).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_right).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_back_right).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_back).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_back_left).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_left).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_front_left).setVisibility(View.INVISIBLE);
                break;
            case OmniWearHelper.DEVICETYPE_ERROR:
                // Hide everything.
                hideButtons();
                break;
        }
    }

    // Set the status message on the screen.
    private void setStatusMessage(String msg) {
        TextView textView = (TextView) findViewById(R.id.connection_status);
        textView.setText(msg);
    }

    // Set the button text.
    private void setButtonText(String msg) {
        Button pairingButton = (Button) findViewById(R.id.button_pairing);
        pairingButton.setText(msg);
    }

    // Check if there's a saved MAC.
    private String getSaved_mac() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(SAVED_MAC_PREF_NAME, "");
    }

    // Set the MAC.
    private void setSaved_mac(String mac) {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SAVED_MAC_PREF_NAME, mac);
        editor.apply();
    }

    // Hide all the buttons.
    private void hideButtons() {
        // Hide everything.
        findViewById(R.id.button_middle_back).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_middle_front).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_middle_left).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_middle_right).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_top).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_front).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_front_right).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_right).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_back_right).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_back).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_back_left).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_left).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_front_left).setVisibility(View.INVISIBLE);
    }
}