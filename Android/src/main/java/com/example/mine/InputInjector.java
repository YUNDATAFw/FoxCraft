package com.example.mine;

import android.app.Instrumentation;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;


public class InputInjector {
    
    private static Instrumentation instrumentation = new Instrumentation();
    
    
    public static void injectKeyEvent(final int keyCode, final int action) {
        
        new Thread(() -> {
            try {
                
                long eventTime = SystemClock.uptimeMillis();
                
                
                KeyEvent keyEvent = new KeyEvent(
                        eventTime,       
                        eventTime,       
                        action,  
                        keyCode,         
                        0,               
                        0,               
                        KeyEvent.KEYCODE_UNKNOWN,  
                        0,               
                        0,               
                        InputDevice.SOURCE_KEYBOARD  
                );
                
                

                    instrumentation.sendKeySync(keyEvent);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    
    public static void injectTouchEvent(final int x, final int y, final boolean down) {
        new Thread(() -> {
            try {
                long eventTime = SystemClock.uptimeMillis();
                
                
                MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
                MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[1];
                
                
                properties[0] = new MotionEvent.PointerProperties();
                properties[0].id = 0;  
                properties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;  
                
                
                coords[0] = new MotionEvent.PointerCoords();
                coords[0].x = x;
                coords[0].y = y;
                coords[0].pressure = 1.0f;  
                coords[0].size = 1.0f;      
                
                
                MotionEvent motionEvent = MotionEvent.obtain(
                        eventTime,
                        eventTime,
                        down ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP,
                        1,  
                        properties,
                        coords,
                        0,  
                        0,  
                        1.0f,  
                        1.0f,  
                        0,  
                        0,  
                        InputDevice.SOURCE_TOUCHSCREEN,  
                        0   
                );
                
                
                instrumentation.sendPointerSync(motionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    
    public static void injectTouchMoveEvent(final int x, final int y) {
        new Thread(() -> {
            try {
                long eventTime = SystemClock.uptimeMillis();
                
                
                MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
                MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[1];
                
                properties[0] = new MotionEvent.PointerProperties();
                properties[0].id = 0;
                properties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
                
                coords[0] = new MotionEvent.PointerCoords();
                coords[0].x = x;
                coords[0].y = y;
                coords[0].pressure = 1.0f;
                coords[0].size = 1.0f;
                
                MotionEvent motionEvent = MotionEvent.obtain(
                        eventTime,
                        eventTime,
                        MotionEvent.ACTION_MOVE,  
                        1,
                        properties,
                        coords,
                        0,
                        0,
                        1.0f,
                        1.0f,
                        0,
                        0,
                        InputDevice.SOURCE_TOUCHSCREEN,
                        0
                );
                
                instrumentation.sendPointerSync(motionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
