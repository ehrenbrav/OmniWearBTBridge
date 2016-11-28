package com.omniwearhaptics.omniwearbtbridge;

import android.view.MotionEvent;
import android.view.View;

import com.omniwearhaptics.omniwearbtbridge.logger.Log;

/**
 * Controlls the actual motors.
 * Created by ehrenbrav on 11/27/16.
 */

class OmniWearDevice {

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


    OmniWearDevice (final MainActivity activity, final OmniWearBluetoothService btService) {

        // Set up touch listeners on the motor buttons.
        activity.findViewById(R.id.button_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(FRONT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(FRONT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(FRONT_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(FRONT_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(BACK_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(BACK_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(BACK), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(BACK), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(BACK_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(BACK_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(FRONT_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(FRONT_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(MID_FRONT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(MID_FRONT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(MID_RIGHT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(MID_RIGHT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(MID_BACK), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(MID_BACK), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(MID_LEFT), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(MID_LEFT), ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_top).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(motor(TOP), OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(motor(TOP), ON); }
                return false;
            }
        });
    }

    private static byte motor(int motor_location) {

        if (MainActivity.getDeviceType() == MainActivity.CAP) {

            switch(motor_location) {
                case FRONT:
                    return 0x0;
                case BACK:
                    return 0x1;
                case RIGHT:
                    return 0x2;
                case LEFT:
                    return 0x3;
                case FRONT_RIGHT:
                    return 0x4;
                case FRONT_LEFT:
                    return 0x5;
                case BACK_RIGHT:
                    return 0x6;
                case BACK_LEFT:
                    return 0x7;
                case MID_FRONT:
                    return 0x8;
                case MID_BACK:
                    return 0x9;
                case MID_RIGHT:
                    return 0xa;
                case MID_LEFT:
                    return 0xb;
                case TOP:
                    return 0xc;
                default:
                    Log.e(TAG, "Invalid motor_location in motor function");
                    return 0x0;
            }
        } else if (MainActivity.getDeviceType() == MainActivity.NECKBAND) {
            return (byte) motor_location;
        }

        // Error.
        Log.e(TAG, "Invalid device type in motor function");
        return 0;
    }
}
