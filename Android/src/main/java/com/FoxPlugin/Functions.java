package com.FoxPlugin;

import android.content.Context;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import com.example.mine.CustomInputMethodService;
import com.example.mine.Service.ShortcutKey;
import com.example.mine.tool.FPPlugin.Utils;
import com.example.mine.tool.KeyboardModel;
import com.example.mine.tool.ShortcutKeyManager;
import com.example.mine.widget.StringToast;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

public class Functions implements ITool {
  @Override
  public void on_IKeyIuputTool(
      Context context,
      String uuid,
      String uid,
      int line,
      int rate,
      String key,
      String onKey,
      JSONArray fnKey,
      int repeat) {

    int useKey;
    if (KeyboardModel.getMode() == KeyboardModel.Mode.FULL_KEYBOARD) {
      useKey = toKey(key);
    } else {
      useKey = toKey(onKey);
    }

    if (useKey == KeyEvent.KEYCODE_UNKNOWN) {
      log(context, "Error", uuid, uid, line, "错误出现在无效的值key/onKey . 在｀on_IKeyIuputTool｀错误已停止此行后续操作");
      return;
    }

    if (rate > 0) {
      ShortcutKey shortcutKey = ShortcutKeyManager.getPluginKeyByUid(uid);
      long interval = 1000L / rate; 
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  long startTime = System.currentTimeMillis(); 
                  int loopCount = 0; 
                  while (shortcutKey.isEnabled()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= interval) {
                      if (fnKey.length() != 0) {
                        int[] keyCodes = new int[fnKey.length()];
                        for (int i = 0; i < fnKey.length(); i++) {
                          int useFnKey;
                          try {
                            useFnKey = toKey(fnKey.getString(i));
                          } catch (JSONException err) {
                            useFnKey = 0;
                          }
                          keyCodes[i] = useFnKey;
                        }
                        CustomInputMethodService.getInstance().sendKeyCombination(useKey, keyCodes);
                      } else {
                        CustomInputMethodService.getInstance().sendKeyCode(useKey);
                      }
                      loopCount++;
                      if (loopCount >= repeat && repeat != -1) break; 
                      startTime = currentTime; 
                    }
                  }
                }
              })
          .start();
    } else if (rate == -1) {
      if (fnKey.length() != 0) {
        int[] keyCodes = new int[fnKey.length()];
        for (int i = 0; i < fnKey.length(); i++) {
          int useFnKey;
          try {
            useFnKey = toKey(fnKey.getString(i));
          } catch (JSONException err) {
            useFnKey = 0;
          }
          keyCodes[i] = useFnKey;
        }
        CustomInputMethodService.getInstance().sendKeyCombination(useKey, keyCodes);
      } else {
        CustomInputMethodService.getInstance().sendKeyCode(useKey);
      }
    } else if (rate == 0) {
      log(context, "Warning", uuid, uid, line, "警告｀rate｀被设置为无效值｀0｀但不终止 . In ｀on_IKeyIuputTool｀");
    }
  }

  @Override
  public void on_Toast(Context context, String message, int duration, int position, int type) {
    StringToast.getInstance(context).show(message, duration, position, type);
  }

  @Override
  public void on_Log() {}

  public static int toKey(String key) {
    Map<String, Integer> keyMap = new HashMap<>();
    
    keyMap.put("_A_", KeyEvent.KEYCODE_A);
    keyMap.put("_B_", KeyEvent.KEYCODE_B);
    keyMap.put("_C_", KeyEvent.KEYCODE_C);
    keyMap.put("_D_", KeyEvent.KEYCODE_D);
    keyMap.put("_E_", KeyEvent.KEYCODE_E);
    keyMap.put("_F_", KeyEvent.KEYCODE_F);
    keyMap.put("_G_", KeyEvent.KEYCODE_G);
    keyMap.put("_H_", KeyEvent.KEYCODE_H);
    keyMap.put("_I_", KeyEvent.KEYCODE_I);
    keyMap.put("_J_", KeyEvent.KEYCODE_J);
    keyMap.put("_K_", KeyEvent.KEYCODE_K);
    keyMap.put("_L_", KeyEvent.KEYCODE_L);
    keyMap.put("_M_", KeyEvent.KEYCODE_M);
    keyMap.put("_N_", KeyEvent.KEYCODE_N);
    keyMap.put("_O_", KeyEvent.KEYCODE_O);
    keyMap.put("_P_", KeyEvent.KEYCODE_P);
    keyMap.put("_Q_", KeyEvent.KEYCODE_Q);
    keyMap.put("_R_", KeyEvent.KEYCODE_R);
    keyMap.put("_S_", KeyEvent.KEYCODE_S);
    keyMap.put("_T_", KeyEvent.KEYCODE_T);
    keyMap.put("_U_", KeyEvent.KEYCODE_U);
    keyMap.put("_V_", KeyEvent.KEYCODE_V);
    keyMap.put("_W_", KeyEvent.KEYCODE_W);
    keyMap.put("_X_", KeyEvent.KEYCODE_X);
    keyMap.put("_Y_", KeyEvent.KEYCODE_Y);
    keyMap.put("_Z_", KeyEvent.KEYCODE_Z);
    
    keyMap.put("_SHIFT_", KeyEvent.KEYCODE_SHIFT_LEFT);
    keyMap.put("_CTRL_", KeyEvent.KEYCODE_CTRL_LEFT);
    keyMap.put("_ALT_", KeyEvent.KEYCODE_ALT_LEFT);
    keyMap.put("_META_", KeyEvent.KEYCODE_META_LEFT);
    keyMap.put("_CAPS_LOCK_", KeyEvent.KEYCODE_CAPS_LOCK);
    keyMap.put("_NUM_LOCK_", KeyEvent.KEYCODE_NUM_LOCK);
    keyMap.put("_SCROLL_LOCK_", KeyEvent.KEYCODE_SCROLL_LOCK);
    keyMap.put("_FN_", KeyEvent.KEYCODE_FUNCTION);

    
    keyMap.put("_0_", KeyEvent.KEYCODE_0);
    keyMap.put("_1_", KeyEvent.KEYCODE_1);
    keyMap.put("_2_", KeyEvent.KEYCODE_2);
    keyMap.put("_3_", KeyEvent.KEYCODE_3);
    keyMap.put("_4_", KeyEvent.KEYCODE_4);
    keyMap.put("_5_", KeyEvent.KEYCODE_5);
    keyMap.put("_6_", KeyEvent.KEYCODE_6);
    keyMap.put("_7_", KeyEvent.KEYCODE_7);
    keyMap.put("_8_", KeyEvent.KEYCODE_8);
    keyMap.put("_9_", KeyEvent.KEYCODE_9);

    
    keyMap.put("_NUMPAD_0_", KeyEvent.KEYCODE_NUMPAD_0);
    keyMap.put("_NUMPAD_1_", KeyEvent.KEYCODE_NUMPAD_1);
    keyMap.put("_NUMPAD_2_", KeyEvent.KEYCODE_NUMPAD_2);
    keyMap.put("_NUMPAD_3_", KeyEvent.KEYCODE_NUMPAD_3);
    keyMap.put("_NUMPAD_4_", KeyEvent.KEYCODE_NUMPAD_4);
    keyMap.put("_NUMPAD_5_", KeyEvent.KEYCODE_NUMPAD_5);
    keyMap.put("_NUMPAD_6_", KeyEvent.KEYCODE_NUMPAD_6);
    keyMap.put("_NUMPAD_7_", KeyEvent.KEYCODE_NUMPAD_7);
    keyMap.put("_NUMPAD_8_", KeyEvent.KEYCODE_NUMPAD_8);
    keyMap.put("_NUMPAD_9_", KeyEvent.KEYCODE_NUMPAD_9);
    keyMap.put("_NUMPAD_DOT_", KeyEvent.KEYCODE_NUMPAD_DOT);
    keyMap.put("_NUMPAD_DIVIDE_", KeyEvent.KEYCODE_NUMPAD_DIVIDE);
    keyMap.put("_NUMPAD_MULTIPLY_", KeyEvent.KEYCODE_NUMPAD_MULTIPLY);
    keyMap.put("_NUMPAD_SUBTRACT_", KeyEvent.KEYCODE_NUMPAD_SUBTRACT);
    keyMap.put("_NUMPAD_ADD_", KeyEvent.KEYCODE_NUMPAD_ADD);
    keyMap.put("_NUMPAD_EQUALS_", KeyEvent.KEYCODE_NUMPAD_EQUALS);
    keyMap.put("_NUMPAD_COMMA_", KeyEvent.KEYCODE_NUMPAD_COMMA);
    keyMap.put("_NUMPAD_LEFT_PAREN_", KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN);
    keyMap.put("_NUMPAD_RIGHT_PAREN_", KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN);

    
    keyMap.put("_COMMA_", KeyEvent.KEYCODE_COMMA);
    keyMap.put("_PERIOD_", KeyEvent.KEYCODE_PERIOD);
    keyMap.put("_SLASH_", KeyEvent.KEYCODE_SLASH);
    keyMap.put("_BACKSLASH_", KeyEvent.KEYCODE_BACKSLASH);
    keyMap.put("_SEMICOLON_", KeyEvent.KEYCODE_SEMICOLON);
    keyMap.put("_APOSTROPHE_", KeyEvent.KEYCODE_APOSTROPHE);
    keyMap.put("_LEFT_BRACKET_", KeyEvent.KEYCODE_LEFT_BRACKET);
    keyMap.put("_RIGHT_BRACKET_", KeyEvent.KEYCODE_RIGHT_BRACKET);
    keyMap.put("_GRAVE_", KeyEvent.KEYCODE_GRAVE);
    keyMap.put("_MINUS_", KeyEvent.KEYCODE_MINUS);
    keyMap.put("_EQUALS_", KeyEvent.KEYCODE_EQUALS);
    keyMap.put("_AT_", KeyEvent.KEYCODE_AT);
    keyMap.put("_STAR_", KeyEvent.KEYCODE_STAR);
    keyMap.put("_PLUS_", KeyEvent.KEYCODE_PLUS);

    return keyMap.getOrDefault(key, KeyEvent.KEYCODE_UNKNOWN);
  }

  private void log(Context context, String type, String uuid, String uid, int i, String err) {

    Utils pluginUtils = new Utils();
    StringBuilder errorMessage = new StringBuilder();
    errorMessage.append("%s出现在<%s/%s>").append("\n");
    errorMessage.append("codeLine:%d").append("\n");
    errorMessage.append("%s -> ").append("\n");
    errorMessage.append("%s").append("\n");
    pluginUtils.writeLogToPlugin(
        context, uuid, uid, errorMessage.toString(), type, uuid, uid, i, type, err);
  }
}
