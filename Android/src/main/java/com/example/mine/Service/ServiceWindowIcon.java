package com.example.mine.Service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.FoxPlugin.Running;
import com.example.mine.Adapter.FunctionListAdapter;
import com.example.mine.Adapter.FunctionListAdapter2;
import com.example.mine.Adapter.XfcHomeItemAdapter;
import com.example.mine.Item.FunctionItem;
import com.example.mine.Item.FunctionItem2;
import com.example.mine.Item.XfcHomeItem;
import com.example.mine.R;
import com.example.mine.tool.FPPlugin.Utils;
import com.example.mine.tool.Function;
import com.example.mine.tool.Max;
import com.example.mine.tool.ShortcutKeyManager;
import com.example.mine.util.GetString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.*;

public class ServiceWindowIcon {
  private Context context;
  private WindowManager windowManager;
  
  private TextView functionNameText;

  // 小悬浮图标（核心）
  private View iconView;
  private WindowManager.LayoutParams iconParams;
  private boolean isIconShowing = false;

  // 主悬浮窗（附属功能）
  private View mainWindowView;
  private WindowManager.LayoutParams mainWindowParams;
  private boolean isMainWindowShowing = false;

  private Function function;
  private DynamicIslandManager islandManager;

  private ShortcutKey shortcutKey_autoClick;
  private ShortcutKey shortcutKey_autoPlace;
  private ShortcutKey shortcutKey_autoJump;
  private ShortcutKey shortcutKey_autoWalk;
  private ShortcutKey shortcutKey_increaseFrequency;
  private ShortcutKey shortcutKey_wsadCycle;

  private ShortcutKey shortcutKey_extract;
  private ShortcutKey shortcutKey_entityCrash;
  private ShortcutKey shortcutKey_dataInjection;

  private FeatureDisplayManager featureManager;
  private FixedWatermarkOverlayManager watermarkManager;
  private Utils pluginUtils;

  public ServiceWindowIcon(Context context) {
    this.context = context.getApplicationContext();
    this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
    this.pluginUtils = new Utils();
  }

  /** 显示小悬浮图标（核心功能入口） */
  public void showIcon() {
    if (isIconShowing || iconView != null) {
      return;
    }

    // 加载图标布局
    iconView = LayoutInflater.from(context).inflate(R.layout.service_window_icon, null);

    // 设置图标参数
    iconParams =
        new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
    iconParams.windowAnimations = R.style.Theme_Red;
    iconParams.gravity = Gravity.TOP | Gravity.LEFT;
    iconParams.x = 100;
    iconParams.y = 100; // 默认位置

    // 添加图标到窗口
    try {
      windowManager.addView(iconView, iconParams);
      isIconShowing = true;
      setupIconEvents(); // 设置图标事件
    } catch (Exception e) {
      e.printStackTrace();
    }

    function = new Function(context);
  }

  /** 设置小悬浮图标的事件（点击和拖拽） */
  private void setupIconEvents() {
    // 图标点击事件 - 切换主悬浮窗显示状态
    iconView.setOnClickListener(v -> toggleMainWindow());

    // 图标拖拽功能
    iconView.setOnTouchListener(
        new View.OnTouchListener() {
          private int initialX;
          private int initialY;
          private float initialTouchX;
          private float initialTouchY;
          private boolean isDragging = false;

          @Override
          public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
              case MotionEvent.ACTION_DOWN:
                initialX = iconParams.x;
                initialY = iconParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                isDragging = false;
                return false;

              case MotionEvent.ACTION_MOVE:
                // 判断是否为拖拽动作
                int deltaX = (int) (event.getRawX() - initialTouchX);
                int deltaY = (int) (event.getRawY() - initialTouchY);
                if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                  isDragging = true;
                  iconParams.x = initialX + deltaX;
                  iconParams.y = initialY + deltaY;
                  windowManager.updateViewLayout(iconView, iconParams);
                }
                return true;

              case MotionEvent.ACTION_UP:
                // 如果是拖拽则消费事件，否则传递给点击事件
                return isDragging;
            }
            return false;
          }
        });
  }

  /** 切换主悬浮窗显示状态 */
  private void toggleMainWindow() {
    if (isMainWindowShowing) {
      hideMainWindow();
    } else {
      showMainWindow();
    }
  }

  /** 显示主悬浮窗 */
  private void showMainWindow() {
    if (isMainWindowShowing || mainWindowView != null) {
      return;
    }

    // 加载主悬浮窗布局
    mainWindowView = LayoutInflater.from(context).inflate(R.layout.service_window, null);

    // 设置主悬浮窗参数
    mainWindowParams =
        new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
    mainWindowParams.windowAnimations = R.style.Theme_Red;
    mainWindowParams.gravity = Gravity.CENTER;
    mainWindowParams.alpha = 0.9f;

    // 添加主悬浮窗到窗口
    try {
      windowManager.addView(mainWindowView, mainWindowParams);
      isMainWindowShowing = true;
      functionNameText = mainWindowView.findViewById(R.id.FunctionName);
      functionNameText.setText("主页");
      loadFunctionLists(); // 加载功能列表
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 主悬浮窗关闭按钮
    LinearLayout closeArea = mainWindowView.findViewById(R.id.coordinator);
    closeArea.setOnClickListener(
        v -> {
          hideMainWindow();
          islandManager.updateContent("Fox");
        });
  }

  /** 隐藏主悬浮窗 */
  private void hideMainWindow() {
    if (isMainWindowShowing && mainWindowView != null) {
      try {
        windowManager.removeView(mainWindowView);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        mainWindowView = null;
        isMainWindowShowing = false;
      }
    }
  }

  /** 加载功能列表数据 */
  private void loadFunctionLists() {
    featureManager = FeatureDisplayManager.getInstance(context);
    loadLeftFunctions();
    loadHomeFunctions();
    islandManager = DynamicIslandManager.getInstance(context);
    islandManager.updateContent("Fox");
    // 获取遮罩
    watermarkManager = FixedWatermarkOverlayManager.getInstance(context);
    loadStatu();
  }

  /* 加载主页页面 */
  private void loadHomeFunctions() {
    List<XfcHomeItem> items = new ArrayList<>();
    items.add(new XfcHomeItem("Item 1"));
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    XfcHomeItemAdapter adapter = new XfcHomeItemAdapter(context, items);
    recyclerView.setAdapter(adapter);
  }

  /** 加载左侧功能列表 */
  private void loadLeftFunctions() {
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));

    List<FunctionItem> functions = new ArrayList<>();
    functions.add(
        new FunctionItem(
            R.drawable.icon_07,
            "主页",
            () -> {
              // 主页功能实现
              islandManager.updateContent("主页");
              functionNameText.setText("主页");
              loadHomeFunctions();
            }));
    functions.add(
        new FunctionItem(
            R.drawable.icon_04,
            "战斗",
            () -> {
              // 战斗功能实现
              islandManager.updateContent("战斗");
              functionNameText.setText("战斗");
              loadFightFunctions();
            }));
    functions.add(
        new FunctionItem(
            R.drawable.icon_05,
            "世界",
            () -> {
              // 世界功能实现
              islandManager.updateContent("世界");
              functionNameText.setText("世界");
              loadWorldFunctions();
            }));
    functions.add(
        new FunctionItem(
            R.drawable.icon_03,
            "视图",
            () -> {
              // 视图功能实现
              islandManager.updateContent("视图");
              functionNameText.setText("视图");
              loadRenderFunctions();
            }));

    functions.add(
        new FunctionItem(
            R.drawable.wool,
            "迷雾",
            () -> {
              // 迷雾功能实现
              islandManager.updateContent("迷雾");
              functionNameText.setText("迷雾");
              loadFogFunctions();
            }));
    /*
     * 插件拓展
     */
    ArrayList<String> uuids = pluginUtils.getPluginListUUID(context); // 获取插件列表
    if (uuids != null) {
      for (int i = 0; i < uuids.size(); i++) {
        String uuid = uuids.get(i);
        String pluginName = pluginUtils.getPluginName(context, uuid);
        functions.add(
            new FunctionItem(
                R.drawable.grass_block,
                pluginName,
                () -> {
                  // 插件功能
                  functionNameText.setText(pluginName);
                  loadPluginFunctions(uuid);
                  islandManager.updateContent(pluginName);
                }));
      }
    }

    recyclerView.setAdapter(new FunctionListAdapter(context, functions));
  }

  private void loadPluginFunctions(final String uuid) {
    // 加载插件功能块
    List<Map<String, Object>> pluginItems = pluginUtils.getPluginItems(context, uuid);

    if (pluginItems == null) return;

    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    // 设置为3列的网格布局
    recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
    List<FunctionItem2> functions = new ArrayList<>();
    
    // 创建一个Map来存储i和pluginUtils.getSubPluginStatus的返回值
    Map<Integer, Boolean> pluginStatusMap = new HashMap<>();

    for (int i = 0; i < pluginItems.size(); i++) {
      Map<String, Object> item = pluginItems.get(i);
      //      Log.d(TAG, "\n第 " + (i + 1) + " 个plugin项:");
      //      Log.d(TAG, "名称: " + item.get("name"));
      //      Log.d(TAG, "ID: " + item.get("id"));
      //      Log.d(TAG, "版本: " + item.get("version"));
      //      Log.d(TAG, "描述: " + item.get("description"));
      //      Log.d(TAG, "模型: " + item.get("model"));
      
      // 获取插件状态并存储到Map中
      boolean status = pluginUtils.getSubPluginStatus(context, uuid, item.get("id").toString());
      pluginStatusMap.put(i, status);

      /* 处理program字段（可能是JSONArray或JSONObject）*/
      Object program = item.get("program");
      JSONArray onOnCode;
      JSONArray onOffCode;
      if (program instanceof JSONObject) {
        JSONObject codeProgram = (JSONObject) program;
        try {
          onOnCode = codeProgram.getJSONArray("on.On");
          onOffCode = codeProgram.getJSONArray("on.Off");
        } catch (JSONException err) {
          onOnCode = new JSONArray();
          onOffCode = new JSONArray();
        }
      } else {
        onOnCode = new JSONArray();
        onOffCode = new JSONArray();
      }
      
      final JSONArray OnCode = onOnCode;
      final JSONArray OffCode = onOffCode;

      functions.add(
          new FunctionItem2(
              R.drawable.icon_29,
              item.get("name").toString(),
              () -> {
                // 点击事件
              },
              () -> {
                /* 开启事件 */
                ShortcutKeyManager.showPluginKey(item.get("id").toString());
                pluginUtils.setSubPluginStatus(context, uuid, item.get("id").toString(), true);
                featureManager.addFeature(item.get("name").toString());
                // onOnCode
                Running running = new Running();
                running.run(context, uuid, item.get("id").toString(), OnCode);
              },
              () -> {
                /* 关闭事件 */
                ShortcutKeyManager.hidePluginKey(item.get("id").toString());
                pluginUtils.setSubPluginStatus(context, uuid, item.get("id").toString(), false);
                featureManager.removeFeature(item.get("name").toString());
                // onOffCode
                Running running = new Running();
                running.run(context, uuid, item.get("id").toString(), OffCode);
              }));
    }
    FunctionListAdapter2 adapter = new FunctionListAdapter2(functions);
    // 加载开关状态
    for (Map.Entry<Integer, Boolean> entry : pluginStatusMap.entrySet()) {
        adapter.setSwitchState(entry.getKey(), entry.getValue());
    }
    recyclerView.setAdapter(adapter);
  }


  private void loadFogFunctions() {
    // 迷雾
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    GetString getString = new GetString();
    String[] returnColor =
        getString.getStringsFromKeywordWrapper(
            context.getApplicationContext(), getString, "FOG_COLOR");
    // 设置为3列的网格布局
    recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
    List<FunctionItem2> functions = new ArrayList<>();
    functions.add(
        new FunctionItem2(
            R.drawable.white_wool,
            "关闭迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[0]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.red_wool,
            "红色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[1]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.yellow_wool,
            "黄色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[2]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.light_blue_wool,
            "蓝色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[3]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.lime_wool,
            "绿色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[4]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.cany_wool,
            "青色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[5]));
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.purple_wool,
            "紫色迷雾",
            () -> {
              // 点击事件
              watermarkManager.setOverlayColor(Color.parseColor(returnColor[6]));
            }));
    FunctionListAdapter2 adapter = new FunctionListAdapter2(functions);
    recyclerView.setAdapter(adapter);
  }

  /** 加载战斗功能列表 */
  private void loadFightFunctions() {
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    // 设置为3列的网格布局
    recyclerView.setLayoutManager(new GridLayoutManager(context, 3));

    shortcutKey_autoClick = ShortcutKeyManager.getAutoClick();
    shortcutKey_autoJump = ShortcutKeyManager.getAutoJump();
    shortcutKey_autoPlace = ShortcutKeyManager.getAutoPlace();
    shortcutKey_autoWalk = ShortcutKeyManager.getAutoWalk();
    shortcutKey_increaseFrequency = ShortcutKeyManager.getIncreaseFrequency();
    shortcutKey_wsadCycle = ShortcutKeyManager.getWsadCycle();

    List<FunctionItem2> functions = new ArrayList<>();
    functions.add(
        new FunctionItem2(
            R.drawable.icon_29,
            "自动点击",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("自动点击");
              shortcutKey_autoClick.show();
              function.setStatus("autoClick", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("自动点击");
              shortcutKey_autoClick.hide();
              function.setStatus("autoClick", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_16,
            "自动放置",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("自动放置");
              shortcutKey_autoPlace.show();
              function.setStatus("autoPlace", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("自动放置");
              shortcutKey_autoPlace.hide();
              function.setStatus("autoPlace", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_13,
            "兔子跳跃",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("自动跳跃");
              shortcutKey_autoJump.show();
              function.setStatus("autoJump", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("自动跳跃");
              shortcutKey_autoJump.hide();
              function.setStatus("autoJump", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_21,
            "自动走路",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("自动行走");
              shortcutKey_autoWalk.show();
              function.setStatus("autoWalk", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("自动行走");
              shortcutKey_autoWalk.hide();
              function.setStatus("autoWalk", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_21,
            "原地踏步",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("原地踏步");
              shortcutKey_wsadCycle.show();
              function.setStatus("wsadCycle", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("原地踏步");
              shortcutKey_wsadCycle.hide();
              function.setStatus("wsadCycle", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_14,
            "点击加倍",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              Max.setMaxXfc(true);
              shortcutKey_increaseFrequency.show();
            },
            () -> {
              /* 关闭事件 */
              Max.setMaxXfc(false);
              shortcutKey_increaseFrequency.hide();
            }));

    boolean autoClickStatus = function.getStatus("autoClick");
    boolean autoPlaceStatus = function.getStatus("autoPlace");
    boolean autoJumpStatus = function.getStatus("autoJump");
    boolean autoWalkStatus = function.getStatus("autoWalk");
    boolean wsadCycleStatus = function.getStatus("wsadCycle");

    FunctionListAdapter2 adapter = new FunctionListAdapter2(functions);
    adapter.setSwitchState(0, autoClickStatus);
    adapter.setSwitchState(1, autoPlaceStatus);
    adapter.setSwitchState(2, autoJumpStatus);
    adapter.setSwitchState(3, autoWalkStatus);
    adapter.setSwitchState(4, wsadCycleStatus);
    adapter.setSwitchState(5, Max.getMaxXfc());
    recyclerView.setAdapter(adapter);
  }

  /* 加载世界功能列表 */
  private void loadWorldFunctions() {
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    // 设置为3列的网格布局
    recyclerView.setLayoutManager(new GridLayoutManager(context, 3));

    // 获取相关的ShortcutKey对象
    shortcutKey_extract = ShortcutKeyManager.getExtract();
    shortcutKey_entityCrash = ShortcutKeyManager.getEntityCrash();
    shortcutKey_dataInjection = ShortcutKeyManager.getDataInjection();

    List<FunctionItem2> functions = new ArrayList<>();
    functions.add(
        new FunctionItem2(
            R.drawable.icon_34,
            "展框拿取",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("展框拿取");
              shortcutKey_extract.show();
              function.setStatus("extract", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("展框拿取");
              shortcutKey_extract.hide();
              function.setStatus("extract", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_33,
            "实体崩服",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("实体崩服");
              shortcutKey_entityCrash.show();
              function.setStatus("entityCrash", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("实体崩服");
              shortcutKey_entityCrash.hide();
              function.setStatus("entityCrash", false);
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.data_node_structure,
            "数据注入",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              featureManager.addFeature("数据注入");
              shortcutKey_dataInjection.show();
              function.setStatus("dataInjection", true);
            },
            () -> {
              /* 关闭事件 */
              featureManager.removeFeature("数据注入");
              shortcutKey_dataInjection.hide();
              function.setStatus("dataInjection", false);
            }));

    // 获取当前状态
    boolean extractStatus = function.getStatus("extract");
    boolean entityCrashStatus = function.getStatus("entityCrash");
    boolean dataInjectionStatus = function.getStatus("dataInjection");

    // 设置适配器
    FunctionListAdapter2 adapter = new FunctionListAdapter2(functions);
    adapter.setSwitchState(0, extractStatus);
    adapter.setSwitchState(1, entityCrashStatus);
    adapter.setSwitchState(2, dataInjectionStatus);
    recyclerView.setAdapter(adapter);
  }

  /* 加载渲染功能列表 */
  private void loadRenderFunctions() {
    RecyclerView recyclerView = mainWindowView.findViewById(R.id.FunctionList2);
    // 设置为3列的网格布局
    recyclerView.setLayoutManager(new GridLayoutManager(context, 3));

    List<FunctionItem2> functions = new ArrayList<>();
    functions.add(
        new FunctionItem2(
            R.drawable.icon_27,
            "显示水印",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              function.setStatus("watermark", true);
              WatermarkFloatWindow.getInstance(context).show();
            },
            () -> {
              /* 关闭事件 */
              function.setStatus("watermark", false);
              WatermarkFloatWindow.getInstance(context).hide();
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_00,
            "灵动通知",
            () -> {
              // 点击事件
            },
            () -> {
              /* 开启事件 */
              function.setStatus("notification", true);
              islandManager.show();
            },
            () -> {
              /* 关闭事件 */
              function.setStatus("notification", false);
              islandManager.hide();
            }));
    functions.add(
        new FunctionItem2(
            R.drawable.icon_14,
            "功能显示",
            () -> {
              // 点击事件

            },
            () -> {
              /* 开启事件 */
              function.setStatus("functionDisplay", true);
              featureManager.setVISIBLE();
            },
            () -> {
              /* 关闭事件 */
              function.setStatus("functionDisplay", false);
              featureManager.setGONE();
            }));

    // 获取当前状态
    boolean watermarkStatus = function.getStatus("watermark");
    boolean notificationStatus = function.getStatus("notification");
    boolean functionDisplayStatus = function.getStatus("functionDisplay");

    if (watermarkStatus) {
      WatermarkFloatWindow.getInstance(context).show();
    } else {
      WatermarkFloatWindow.getInstance(context).hide();
    }

    // 设置适配器
    FunctionListAdapter2 adapter = new FunctionListAdapter2(functions);
    adapter.setSwitchState(0, watermarkStatus);
    adapter.setSwitchState(1, notificationStatus);
    adapter.setSwitchState(2, functionDisplayStatus);
    recyclerView.setAdapter(adapter);
  }

  /** 获取窗口类型，适配不同Android版本 */
  private int getWindowType() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
  }

  /** 隐藏小悬浮图标（核心功能关闭） */
  public void hideIcon() {
    hideMainWindow(); // 先隐藏主窗口
    // 关闭水印
    WatermarkFloatWindow.getInstance(context).hide();
    if (featureManager != null) {
      featureManager.clearAllFeatures();
    }
    if (islandManager != null) {
      islandManager.hide();
    }
    if (isIconShowing && iconView != null) {
      try {
        windowManager.removeView(iconView);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        iconView = null;
        isIconShowing = false;
      }
    }
  }

  /** 判断小悬浮图标是否正在显示 */
  public boolean isIconShowing() {
    return isIconShowing;
  }

  /** 切换小悬浮图标的显示状态 */
  public void toggleIcon() {
    if (isIconShowing()) {
      hideIcon();
    } else {
      showIcon();
    }
  }

  private void isShortcutKey(ShortcutKey key, boolean is) {
    if (key != null) {
      if (is) {
        key.show();
      } else {
        key.hide();
      }
    }
  }

  private void loadStatu() {
    // 加载当前状态
    boolean watermarkStatus = function.getStatus("watermark");
    boolean notificationStatus = function.getStatus("notification");
    boolean functionDisplayStatus = function.getStatus("functionDisplay");
    boolean autoClickStatus = function.getStatus("autoClick");
    boolean autoPlaceStatus = function.getStatus("autoPlace");
    boolean autoJumpStatus = function.getStatus("autoJump");
    boolean autoWalkStatus = function.getStatus("autoWalk");
    boolean extractStatus = function.getStatus("extract");
    boolean entityCrashStatus = function.getStatus("entityCrash");
    boolean dataInjectionStatus = function.getStatus("dataInjection");
    if (notificationStatus) {
      islandManager.show();
    } else {
      islandManager.hide();
    }

    if (functionDisplayStatus) {
      featureManager.show();
    } else {
      featureManager.hide();
    }

    if (autoClickStatus) {
      shortcutKey_autoClick.show();
    }

    if (autoJumpStatus) {
      shortcutKey_autoJump.show();
    }

    if (autoPlaceStatus) {
      shortcutKey_autoPlace.show();
    }

    if (autoWalkStatus) {
      shortcutKey_autoWalk.show();
    }

    if (extractStatus) {
      shortcutKey_extract.show();
    }

    if (entityCrashStatus) {
      shortcutKey_entityCrash.show();
    }

    if (dataInjectionStatus) {
      shortcutKey_dataInjection.show();
    }

    if (Max.getMaxXfc()) {
      shortcutKey_increaseFrequency.show();
    }

    if (watermarkStatus) {
      WatermarkFloatWindow.getInstance(context).hide();
    }
  }
}
