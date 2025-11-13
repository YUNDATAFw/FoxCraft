package com.example.mine.Service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.mine.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 灵动岛风格悬浮窗管理类（单例模式）
 * 自动获取并显示时间 • 电量 • 自定义内容，位于屏幕中上位置
 */
public class DynamicIslandManager {
    private static volatile DynamicIslandManager instance;

    private Context context;
    private WindowManager windowManager;
    private View islandView;
    private TextView timeTextView;
    private TextView batteryTextView;
    private TextView contentTextView;
    private WindowManager.LayoutParams islandParams;
    private boolean isShowing = false;
    private String currentContent = "Fox";
    private Timer updateTimer;
    private BatteryManager batteryManager;

    private DynamicIslandManager(Context context) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        
        // 初始化电池管理器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        }
        
        initLayoutParams();
        initView();
        startAutoUpdate();
    }

    /**
     * 获取单例实例
     * @param context 上下文
     * @return 单例对象
     */
    public static DynamicIslandManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DynamicIslandManager.class) {
                if (instance == null) {
                    instance = new DynamicIslandManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化悬浮窗布局参数
     */
    private void initLayoutParams() {
        islandParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                // 设置为不可点击、不可聚焦，不影响下层操作
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        // 定位到中上位置
        islandParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        // 距离顶部有一定距离，形成中上效果
        islandParams.y = 100;
    }

    /**
     * 初始化悬浮窗视图
     */
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        islandView = inflater.inflate(R.layout.dynamic_island_layout, null);
        
        // 获取三个TextView的引用
        timeTextView = islandView.findViewById(R.id.time_text);
        batteryTextView = islandView.findViewById(R.id.battery_text);
        contentTextView = islandView.findViewById(R.id.content_text);
        
        // 初始化显示内容
        updateTimeDisplay();
        updateBatteryDisplay();
        contentTextView.setText(currentContent);
    }

    /**
     * 启动自动更新计时器
     */
    private void startAutoUpdate() {
        // 每5秒更新一次时间和电量
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 在UI线程更新显示
                new Handler(Looper.getMainLooper()).post(() -> {
                    updateTimeDisplay();
                    updateBatteryDisplay();
                });
            }
        }, 0, 5000);
    }

    /**
     * 更新时间显示
     */
    private void updateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextView.setText(currentTime);
    }

    /**
     * 自动更新电量显示
     */
    private void updateBatteryDisplay() {
        int batteryLevel = getBatteryLevel();
        batteryTextView.setText(batteryLevel + "%");
    }

    /**
     * 获取当前电池电量
     * @return 电量百分比
     */
    private int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 对于API 21及以上，使用BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            // 对于旧版本，使用Intent
            Intent intent = context.registerReceiver(null, 
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent != null) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level != -1 && scale != -1) {
                    return (int) ((level / (float) scale) * 100);
                }
            }
            return -1; // 无法获取电量
        }
    }

    /**
     * 更新自定义内容显示
     * @param content 要显示的自定义内容
     */
    public void updateContent(String content) {
        if (content == null) return;
        
        currentContent = content;
        if (contentTextView != null) {
            contentTextView.setText(currentContent);
        }
    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (isShowing || islandView == null) return;

        try {
            if (islandView.getParent() == null) {
                windowManager.addView(islandView, islandParams);
            }
            islandView.setVisibility(View.VISIBLE);
            isShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏悬浮窗
     */
    public void hide() {
        if (!isShowing || islandView == null) return;

        try {
            if (islandView.getParent() != null) {
                windowManager.removeView(islandView);
            }
            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查悬浮窗是否正在显示
     * @return 是否显示
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 释放资源，退出时调用
     */
    public void release() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        hide();
        instance = null;
    }
}
    