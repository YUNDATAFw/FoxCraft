package com.example.mine.tool;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.FoxPlugin.Running;
import com.example.mine.CustomInputMethodService;
import com.example.mine.Service.FeatureDisplayManager;
import com.example.mine.Service.ShortcutKey;
import com.example.mine.tool.FPPlugin.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.*;
import java.util.concurrent.TimeUnit;


public class ShortcutKeyManager {
  private static Utils pluginUtils;
  
  private static ShortcutKey shortcutKey_autoClick;
  private static ShortcutKey shortcutKey_autoPlace;
  private static ShortcutKey shortcutKey_autoJump;
  private static ShortcutKey shortcutKey_autoWalk;

  private static ShortcutKey shortcutKey_extract;
  private static ShortcutKey shortcutKey_entityCrash;
  private static ShortcutKey shortcutKey_dataInjection;

  private static ShortcutKey shortcutKey_increaseFrequency;
  private static ShortcutKey shortcutKey_wsadCycle;

  private static Map<String, ShortcutKey> pluginKeyMap = new HashMap<>();

  private static Thread loopThread_autoClick;
  private static Thread loopThread_autoPlace;
  private static Thread loopThread_autoJump;

  private static Thread loopThread_entityCrash;
  
  private static Thread loopThread_wsadCycle;

  private static Function function;
  private static FeatureDisplayManager featureManager;

  public static void initShortcutKey(Context context) {

    pluginUtils = new Utils();
    
    featureManager = FeatureDisplayManager.getInstance(context);

    shortcutKey_autoClick =
        new ShortcutKey(
            context,
            "自动点击",
            ShortcutKey.Mode.SWITCH,
            () -> {
              loopThread_autoClick =
                  loopThread(KeyEvent.KEYCODE_Q, shortcutKey_autoClick, Max.get()[0]);
              
              loopThread_autoClick.start();
            },
            () -> {
              

              if (loopThread_autoClick != null) {
                loopThread_autoClick.interrupt();
              }
            });

    shortcutKey_autoPlace =
        new ShortcutKey(
            context,
            "自动放置",
            ShortcutKey.Mode.SWITCH,
            () -> {
              loopThread_autoPlace =
                  loopThread(KeyEvent.KEYCODE_E, shortcutKey_autoPlace, Max.get()[1]);
              
              loopThread_autoPlace.start();
            },
            () -> {
              

              if (loopThread_autoPlace != null) {
                loopThread_autoPlace.interrupt();
              }
            });

    shortcutKey_autoJump =
        new ShortcutKey(
            context,
            "自动跳跃",
            ShortcutKey.Mode.SWITCH,
            () -> {
              loopThread_autoJump =
                  loopThread(KeyEvent.KEYCODE_SPACE, shortcutKey_autoJump, Max.get()[2]);
              
              loopThread_autoJump.start();
            },
            () -> {
              

              if (loopThread_autoJump != null) {
                loopThread_autoJump.interrupt();
              }
            });

    shortcutKey_autoWalk =
        new ShortcutKey(
            context,
            "自动行走",
            ShortcutKey.Mode.SWITCH,
            () -> {
              CustomInputMethodService.getInstance().keyDown(KeyEvent.KEYCODE_W);
            },
            () -> {
              CustomInputMethodService.getInstance().keyUp(KeyEvent.KEYCODE_W);
            });

    shortcutKey_extract =
        new ShortcutKey(
            context,
            "展框拿取",
            ShortcutKey.Mode.BUTTON,
            () -> {
              CustomInputMethodService.getInstance().sendKeyCode(KeyEvent.KEYCODE_Q);
            });

    shortcutKey_dataInjection =
        new ShortcutKey(
            context,
            "数据注入",
            ShortcutKey.Mode.BUTTON,
            () -> {
              CustomInputMethodService.getInstance().sendCtrlKey(KeyEvent.KEYCODE_X);
            });

    shortcutKey_entityCrash =
        new ShortcutKey(
            context,
            "实体崩服",
            ShortcutKey.Mode.SWITCH,
            () -> {
              loopThread_entityCrash =
                  loopThread(KeyEvent.KEYCODE_E, shortcutKey_entityCrash, Max.get()[3]);

              
              loopThread_entityCrash.start();
            },
            () -> {
              
              if (loopThread_entityCrash != null) {
                loopThread_entityCrash.interrupt();
              }
            });

    shortcutKey_increaseFrequency =
        new ShortcutKey(
            context,
            "点击加倍",
            ShortcutKey.Mode.SWITCH,
            () -> {
              Max.setMax(true);
            },
            () -> {
              Max.setMax(false);
            });
    

    shortcutKey_wsadCycle = new ShortcutKey(
        context,
        "原地踏步",
        ShortcutKey.Mode.SWITCH,
        () -> {
            
            loopThread_wsadCycle = new Thread(() -> {
                int[] keys = {
                    KeyEvent.KEYCODE_W,
                    KeyEvent.KEYCODE_A,
                    KeyEvent.KEYCODE_S,
                    KeyEvent.KEYCODE_D
                };
                int interval = 250; 
                
                while (shortcutKey_wsadCycle.isEnabled()) {
                    for (int key : keys) {
                        
                        if (!shortcutKey_wsadCycle.isEnabled()) break;
                        
                        CustomInputMethodService.getInstance().sendKeyCode(key);
                        
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            });
            loopThread_wsadCycle.start();
        },
        () -> {
            
            if (loopThread_wsadCycle != null) {
                loopThread_wsadCycle.interrupt();
                loopThread_wsadCycle = null;
            }
        }
    );

    ArrayList<String> uuids = pluginUtils.getPluginListUUID(context); 
    if (uuids != null) {
      for (int i = 0; i < uuids.size(); i++) {
        String uuid = uuids.get(i);
        List<Map<String, Object>> pluginItems = pluginUtils.getPluginItems(context, uuid);
        if (pluginItems == null) break;
        for (int ii = 0; ii < pluginItems.size(); ii++) {
          Map<String, Object> item = pluginItems.get(ii);
          ShortcutKey.Mode model = ShortcutKey.Mode.BUTTON;
          switch(item.get("model").toString()){
            case "_Style.SWITCH_":
            model = ShortcutKey.Mode.SWITCH;
            break;
            case "_Style.Button_":
            model = ShortcutKey.Mode.BUTTON;
            break;
          }

          Object program = item.get("program");
          JSONObject codeProgram;
          if (program instanceof JSONObject) {
             codeProgram = ((JSONObject) program);
          } else {
             codeProgram = new JSONObject();
          }

          addPluginKey(
              context,uuid, item.get("id").toString(), item.get("name").toString(), model, codeProgram);
          if(pluginUtils.getSubPluginStatus(context, uuid, item.get("id").toString())){
            showPluginKey(item.get("id").toString());
          } else {
            hidePluginKey(item.get("id").toString());
          }
        }
      }
    }

    function = new Function(context);
    boolean autoClickStatus = function.getStatus("autoClick");
    boolean autoPlaceStatus = function.getStatus("autoPlace");
    boolean autoJumpStatus = function.getStatus("autoJump");
    boolean autoWalkStatus = function.getStatus("autoWalk");
    boolean wsadCycleStatus = function.getStatus("wsadCycle");

    isShortcutKey(shortcutKey_autoClick, autoClickStatus);
    isShortcutKey(shortcutKey_autoJump, autoJumpStatus);
    isShortcutKey(shortcutKey_autoPlace, autoPlaceStatus);
    isShortcutKey(shortcutKey_autoWalk, autoWalkStatus);
    isShortcutKey(shortcutKey_increaseFrequency, Max.getMaxXfc());
    isShortcutKey(shortcutKey_wsadCycle, wsadCycleStatus);
    
    
    boolean extractStatus = function.getStatus("extract");
    boolean entityCrashStatus = function.getStatus("entityCrash");
    boolean dataInjectionStatus = function.getStatus("dataInjection");

    
    isShortcutKey(shortcutKey_extract, extractStatus);
    isShortcutKey(shortcutKey_entityCrash, entityCrashStatus);
    isShortcutKey(shortcutKey_dataInjection, dataInjectionStatus);
  }

  private static ShortcutKey addPluginKey(
      Context context,String uuid, String uid, String name, ShortcutKey.Mode type,JSONObject program) {
    JSONArray otherOnCode, otherOffCode,otherActivate;
    try {
      otherOnCode = program.getJSONArray("other.On");
      otherOffCode = program.getJSONArray("other.Off");
      otherActivate = program.getJSONArray("other.Activate");
    } catch (JSONException e) {
      otherOnCode = new JSONArray();
      otherOffCode = new JSONArray();
      otherActivate = new JSONArray();
    }
    final JSONArray onCode = otherOnCode;
    final JSONArray offCode = otherOffCode;
    final JSONArray onActivate = otherActivate;
    ShortcutKey pluginKey;
    if (type == ShortcutKey.Mode.SWITCH) {
      pluginKey =
          new ShortcutKey(
              context,
              name, 
              type, 
              () -> {
                
                Running running = new Running();
                running.run(context, uuid, uid, onCode);
              },
              () -> {
                
                Running running = new Running();
                running.run(context, uuid, uid, offCode);
              });
    } else{
      pluginKey =
          new ShortcutKey(
              context,
              name, 
              type, 
              () -> {
                
                Running running = new Running();
                running.run(context, uuid, uid, onActivate);
              });
    }
    pluginKeyMap.put(uid, pluginKey); 
    return pluginKey;
  }

  public static ShortcutKey getAutoClick() {
    return shortcutKey_autoClick;
  }

  public static ShortcutKey getAutoPlace() {
    return shortcutKey_autoPlace;
  }

  public static ShortcutKey getAutoJump() {
    return shortcutKey_autoJump;
  }

  public static ShortcutKey getAutoWalk() {
    return shortcutKey_autoWalk;
  }

  public static ShortcutKey getExtract() {
    return shortcutKey_extract;
  }

  public static ShortcutKey getEntityCrash() {
    return shortcutKey_entityCrash;
  }

  public static ShortcutKey getDataInjection() {
    return shortcutKey_dataInjection;
  }

  public static ShortcutKey getIncreaseFrequency() {
    return shortcutKey_increaseFrequency;
  }
  
  public static ShortcutKey getWsadCycle() {
    return shortcutKey_wsadCycle;
}

  public static void killShortcutKey() {
    
    if (pluginKeyMap != null) {
      for (ShortcutKey key : pluginKeyMap.values()) {
        key.hide(); 
      }
      pluginKeyMap.clear(); 
    }
    if (shortcutKey_autoClick != null) {
      shortcutKey_autoClick.hide();
      if (loopThread_autoClick != null) {
        loopThread_autoClick.interrupt();
      }
      loopThread_autoClick = null;
      shortcutKey_autoClick = null;
    }
    if (shortcutKey_autoPlace != null) {
      shortcutKey_autoPlace.hide();
      if (loopThread_autoPlace != null) {
        loopThread_autoPlace.interrupt();
      }
      loopThread_autoPlace = null;
      shortcutKey_autoPlace = null;
    }
    if (shortcutKey_autoJump != null) {
      shortcutKey_autoJump.hide();
      if (loopThread_autoJump != null) {
        loopThread_autoJump.interrupt();
      }
      loopThread_autoJump = null;
      shortcutKey_autoJump = null;
    }
    if (shortcutKey_autoWalk != null) {
      shortcutKey_autoWalk.hide();
      shortcutKey_autoWalk = null;
    }
    if (shortcutKey_extract != null) {
      shortcutKey_extract.hide();
      shortcutKey_extract = null;
    }
    if (shortcutKey_entityCrash != null) {
      shortcutKey_entityCrash.hide();
      if (loopThread_entityCrash != null) {
        loopThread_entityCrash.interrupt();
      }
      loopThread_entityCrash = null;
      shortcutKey_entityCrash = null;
    }
    if (shortcutKey_dataInjection != null) {
      shortcutKey_dataInjection.hide();
      shortcutKey_dataInjection = null;
    }

    if (shortcutKey_increaseFrequency != null) {
      shortcutKey_increaseFrequency.hide();
      shortcutKey_increaseFrequency = null;
    }
    
    if (shortcutKey_wsadCycle != null) {
        shortcutKey_wsadCycle.hide();
        if (loopThread_wsadCycle != null) {
            loopThread_wsadCycle.interrupt();
        }
        loopThread_wsadCycle = null;
        shortcutKey_wsadCycle = null;
    }
  }

  private static Thread loopThread(int key, ShortcutKey shortcutKey, int m) {
    final long interval = Math.max(1, 1000L / m); 
    return new Thread(
        () -> {
          long lastSendTime = System.nanoTime(); 
          while (shortcutKey.isEnabled()) {
            CustomInputMethodService.getInstance().sendKeyCode(key);
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastSendTime;
            long sleepTime = interval - TimeUnit.NANOSECONDS.toMillis(elapsedTime);
            if (sleepTime > 0) {
              try {
                Thread.sleep(sleepTime); 
              } catch (InterruptedException e) {
                e.printStackTrace();
                break; 
              }
            }
            lastSendTime = currentTime;
          }
        });
}


  private static void isShortcutKey(ShortcutKey key, boolean is) {
    if (key != null) {
      if (is) {
        key.show();
      } else {
        key.hide();
      }
    }
  }

  
  public static ShortcutKey getPluginKeyByUid(String uid) {
    return pluginKeyMap.get(uid);
  }

  
  public static void showPluginKey(String uid) {
    ShortcutKey key = pluginKeyMap.get(uid);
    if (key != null) {
      key.show();
    }
  }

  
  public static void hidePluginKey(String uid) {
    ShortcutKey key = pluginKeyMap.get(uid);
    if (key != null) {
      key.hide();
    }
  }
}
