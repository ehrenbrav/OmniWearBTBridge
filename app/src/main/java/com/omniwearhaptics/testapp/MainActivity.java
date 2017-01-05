package com.omniwearhaptics.testapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
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
 

    private boolean mLogShown = false;
    private static int mDeviceType = OmniWearHelper.DEVICETYPE_NECKBAND;

    private OmniWearHelper mHelper;
    private static OmniWearDevice mOmniwearButtons = null;

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
                mOmniwearButtons.setmIntensity(intensity);
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
        RadioButton radioButton = (RadioButton) findViewById(R.id.radio_neckband);
        radioButton.performClick();
        mDeviceType = OmniWearHelper.DEVICETYPE_NECKBAND;
        Button logButton = (Button) findViewById(R.id.button_toggle_log);
        logButton.setText(R.string.show_log);
        ViewAnimator logFragment = (ViewAnimator) findViewById(R.id.log_fragment_animator);
        logFragment.setVisibility(View.INVISIBLE);

        setButtonText(getString(R.string.pair_device));
    }

    @Override
    public void onPause() {
        super.onPause();
        mHelper.shutdown();
        // Force quitting the app since we should actually be disabling all the buttons here (but we aren't).
        // If we didn't force quit here, we'd have a crash later on if we tried to use the buttons before pairing.
        finish();
    }

    // Handle forgetting and pairing of the device.
    public void onPairingButtonClicked(View view) {
        Button pairingButton = (Button) findViewById(R.id.button_pairing);
        pairingButton.setEnabled(false);
    	mHelper = new OmniWearHelper(this, mDeviceType, new Runnable(){
			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
			            setStatusMessage(getString(R.string.status_connected));
					}
				});
			}
    	});
    	mOmniwearButtons = new OmniWearDevice(this, mHelper);

    	setStatusMessage(getString(R.string.status_searching));
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

    // Hides or shows the buttons that differ between Cap and Neckband.
    public void onRadioButtonClicked(View view) {

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_cap:
                if (checked)
                    // Show cap-only buttons.
                    findViewById(R.id.button_middle_back).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_middle_front).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_middle_left).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_middle_right).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_top).setVisibility(View.VISIBLE);
                    mDeviceType = OmniWearHelper.DEVICETYPE_CAP;

                break;
            case R.id.radio_neckband:
                if (checked)
                    // Hide cap-only buttons.
                    findViewById(R.id.button_middle_back).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_front).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_left).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_right).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_top).setVisibility(View.INVISIBLE);
                    mDeviceType =  OmniWearHelper.DEVICETYPE_NECKBAND;
                    break;
        }
    }

    // Set the status message on the screen.
    public void setStatusMessage(String msg) {
        TextView textView = (TextView) findViewById(R.id.connection_status);
        textView.setText(msg);
    }

    // Set the button text.
    public void setButtonText(String msg) {
        Button pairingButton = (Button) findViewById(R.id.button_pairing);
        pairingButton.setText(msg);
    }

    // Get the type of device selected.
    public static int getDeviceType() {
        return mDeviceType;
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
}
