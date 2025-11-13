package com.example.mine;

import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.InputDevice;

public class CustomInputMethodService extends InputMethodService {
    
    private static CustomInputMethodService instance;
    
    private InputConnection inputConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;
    }

    
    public static CustomInputMethodService getInstance() {
        return instance;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        inputConnection = getCurrentInputConnection();
    }

    
    public void commitText(String text) {
        if (inputConnection != null) {
            inputConnection.commitText(text, 1);
        }
    }

    
    public void deleteText() {
        if (inputConnection != null) {
            inputConnection.deleteSurroundingText(1, 0);
        }
    }

    
    public void sendKeyCode(int keyCode) {
        sendKeyEvent(keyCode, 0); 
    }

    
    public void sendCtrlKey(int keyCode) {
        
        sendKeyEvent(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_DOWN);
        
        
        sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
        sendKeyEvent(keyCode, KeyEvent.ACTION_UP);
        
        
        sendKeyEvent(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_UP);
    }

  public void sendKeyCombination(int targetKeyCode, int... modifierKeys) {
    
    for (int modifierKey : modifierKeys) {
      sendKeyEvent(modifierKey, KeyEvent.ACTION_DOWN);
    }

    
    sendKeyEvent(targetKeyCode, KeyEvent.ACTION_DOWN);
    sendKeyEvent(targetKeyCode, KeyEvent.ACTION_UP);

    
    for (int modifierKey : modifierKeys) {
      sendKeyEvent(modifierKey, KeyEvent.ACTION_UP);
    }
  }

    
    public void keyDown(int keyCode) {
        sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
    }

    
    public void keyUp(int keyCode) {
        sendKeyEvent(keyCode, KeyEvent.ACTION_UP);
    }

    
    private void sendKeyEvent(int keyCode, int action) {
        if (inputConnection == null) return;

        
        long eventTime = SystemClock.uptimeMillis();
        
        
        KeyEvent keyEvent = new KeyEvent(
                eventTime,       
                eventTime,       
                action,          
                keyCode,         
                0,               
                0,               
                KeyCharacterMap.VIRTUAL_KEYBOARD, 
                0,               
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE,
                InputDevice.SOURCE_KEYBOARD
        );
        
        
        inputConnection.sendKeyEvent(keyEvent);
    }

    
    public boolean sendTest() {
        try {
            getInstance().sendKeyCode(KeyEvent.KEYCODE_SPACE);
            return true;
        } catch (Exception e) {
            Log.e("CustomInputMethodService", "sendSpace failed", e);
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        instance = null;
    }
}
