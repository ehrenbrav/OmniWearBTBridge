package com.omniwearhaptics.testapp;

import android.view.MotionEvent;
import android.view.View;

import com.omniwearhaptics.api.OmniWearHelper;
import com.omniwearhaptics.omniwearbtbridge.R;
import com.omniwearhaptics.omniwearbtbridge.R.id;
import com.omniwearhaptics.omniwearbtbridge.logger.Log;

/**
 * Controlls the actual motors.
 * Created by ehrenbrav on 11/27/16.
 */

public class OmniWearDevice {

	// It's odd to have a helper class constructor set up the UI of your Activity.
	// Also, these constants should be changed to using the ones in OmniWearHelper to ensure the values stay in sync.
	// I'm leaving this as close to the original as possible to make my changes less scary, but this file really should be refactored.
    private static final String TAG = "OmniWearDevice";

    private static final int FRONT =        0;
    private static final int BACK =         1;
    private static final int RIGHT =        2;
    private static final int LEFT =         3;
    private static final int FRONT_RIGHT =  4;
    private static final int FRONT_LEFT =   5;
    private static final int BACK_RIGHT =   6;
    private static final int BACK_LEFT =    7;
    private static final int MID_FRONT =    8;
    private static final int MID_RIGHT =    9;
    private static final int MID_BACK =     10;
    private static final int MID_LEFT =     11;
    private static final int TOP =          12;

    private static final byte OFF =          0;
    private static final byte ON =           100;


    public OmniWearDevice (final MainActivity activity, final OmniWearHelper btService) {

        // Set up touch listeners on the motor buttons.
        activity.findViewById(R.id.button_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((FRONT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((FRONT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((FRONT_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((FRONT_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((BACK_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((BACK_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((BACK), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((BACK), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((BACK_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((BACK_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((FRONT_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((FRONT_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((MID_FRONT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((MID_FRONT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((MID_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((MID_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((MID_BACK), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((MID_BACK), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor((MID_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor((MID_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_top).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.setMotor(TOP, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.setMotor(TOP, ON); }
                return false;
            }
        });
    }
}
