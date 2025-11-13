package com.example.mine;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.InputDevice;
public class CustomKeyCodeImeService extends InputMethodService {
    
    private boolean isCtrlPressed = false;
    
    public void sendKeyCode(int keyCode) {
        sendKeyEvent(keyCode, 0); 
    }
    
    public void sendCtrlKey(int keyCode) {
        
        if (!isCtrlPressed) {
            sendKeyEvent(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_DOWN);
            isCtrlPressed = true;
        }
        
        
        sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
        sendKeyEvent(keyCode, KeyEvent.ACTION_UP);
        
        
        sendKeyEvent(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_UP);
        isCtrlPressed = false;
    }
    
    private void sendKeyEvent(int keyCode, int action) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        
        long eventTime = SystemClock.uptimeMillis();
        
        
        int metaState = isCtrlPressed ? KeyEvent.META_CTRL_ON : 0;
        KeyEvent keyEvent = new KeyEvent(
                eventTime,       
                eventTime,       
                action,          
                keyCode,         
                0,               
                metaState,       
                KeyCharacterMap.VIRTUAL_KEYBOARD, 
                0,               
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE,
                InputDevice.SOURCE_KEYBOARD
        );
        
        
        ic.sendKeyEvent(keyEvent);
    }
    
    public void handleExternalCommand(int command) {
        switch (command) {
            case 1: 
                sendCtrlKey(KeyEvent.KEYCODE_C);
                break;
            case 2: 
                sendKeyCode(KeyEvent.KEYCODE_Q);
                break;
            
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
