package com.omniwearhaptics.omniwearbtbridge;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

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

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 3;
    private final OmniWearHandler mHandler = new OmniWearHandler(this);
    private boolean mLogShown;
    private static OmniWearBluetoothService mBTManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBTManager = new OmniWearBluetoothService(this, mHandler);

        // Android M Permission check 
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can do a BlueTooth search.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    //requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
            });
            builder.show();
        }
    }

    // Handle getting permissions to do a BT search.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not work.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // See if BT is enabled.
        mBTManager.resume(this);

        // Set default options.
        TextView textView = (TextView) findViewById(R.id.connection_status);
        textView.setText(R.string.status_not_connected);
        RadioButton radioButton = (RadioButton) findViewById(R.id.radio_neckband);
        radioButton.performClick();

        // If no device is known, ask to pair.
        Button pairingButton = (Button) findViewById(R.id.button_pairing);
        if (OmniWearBluetoothService.getSaved_mac().equals("")) {
            pairingButton.setText(R.string.pair_device);
        } else {
            pairingButton.setText(R.string.forget_device);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBTManager.stop();
    }

    // Handle forgetting and pairing of the device.
    public void onPairingButtonClicked(View view) {

        if (OmniWearBluetoothService.getSaved_mac().equals("")) {

            // Look for the OmniWear Device.
            mBTManager.search(getApplicationContext());
        } else {

            // Forget the saved MAC.
            OmniWearBluetoothService.setSaved_mac("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

                break;
            case R.id.radio_neckband:
                if (checked)
                    // Hide cap-only buttons.
                    findViewById(R.id.button_middle_back).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_front).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_left).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_middle_right).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_top).setVisibility(View.INVISIBLE);
                    break;
        }
    }

    // Set the status message on the screen.
    public void setStatusMessage(String msg) {
        TextView textView = (TextView) findViewById(R.id.connection_status);
        textView.setText(msg);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? "Hide Log" : "Show Log");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
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
