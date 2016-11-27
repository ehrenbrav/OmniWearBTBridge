package com.omniwearhaptics.omniwearbtbridge;

import android.view.MotionEvent;
import android.view.View;

/**
 * Controlls the actual motors.
 * Created by ehrenbrav on 11/27/16.
 */

class OmniWearDevice {

    private static final byte FRONT =        0;
    private static final byte BACK =         1;
    private static final byte RIGHT =        2;
    private static final byte LEFT =         3;
    private static final byte FRONT_RIGHT =  4;
    private static final byte FRONT_LEFT =   5;
    private static final byte BACK_RIGHT =   6;
    private static final byte BACK_LEFT =    7;
    private static final byte MID_FRONT =    0;
    private static final byte MID_RIGHT =    0;
    private static final byte MID_BACK =     0;
    private static final byte MID_LEFT =     0;
    private static final byte TOP =          0;
    private static final byte OFF =          0;
    private static final byte ON =           100;


    OmniWearDevice (final MainActivity activity, final OmniWearBluetoothService btService) {

        // Set up touch listeners on the motor buttons.
        activity.findViewById(R.id.button_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(FRONT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(FRONT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(FRONT_RIGHT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(FRONT_RIGHT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(RIGHT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(RIGHT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(BACK_RIGHT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(BACK_RIGHT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(BACK, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(BACK, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_back_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(BACK_LEFT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(BACK_LEFT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(LEFT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(LEFT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_front_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(FRONT_LEFT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(FRONT_LEFT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_front).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(MID_FRONT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(MID_FRONT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_right).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(MID_RIGHT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(MID_RIGHT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(MID_BACK, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(MID_BACK, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_middle_left).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(MID_LEFT, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(MID_LEFT, ON); }
                return false;
            }
        });
        activity.findViewById(R.id.button_top).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { btService.commandMotor(TOP, OFF); }
                if (event.getAction() == MotionEvent.ACTION_DOWN) { btService.commandMotor(TOP, ON); }
                return false;
            }
        });
    }
}
