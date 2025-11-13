package com.example.mine.Service;

import android.content.Context;
import android.graphics.PixelFormat;
import com.example.mine.util.*;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.mine.R;
import com.example.mine.util.AssetsFileUtils;
import com.example.mine.util.*;
import java.io.IOException;
import android.widget.FrameLayout;

public class WatermarkFloatWindow {
  private static volatile WatermarkFloatWindow instance;
  private final WindowManager windowManager;
  private final WindowManager.LayoutParams windowParams;
  private final View view;
  private TextView titleTextView;
  private TextView textTextView;
  private TextView messageTextView;

  // 私有构造函数，确保只能通过getInstance获取实例
  private WatermarkFloatWindow(Context context) {
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    // 初始化布局参数
    windowParams =
        new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 悬浮窗的类型
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT // 透明度设置
            );

    // 设置位置和大小
    windowParams.gravity = Gravity.TOP | Gravity.END;
    windowParams.alpha = 0.6f;
    windowParams.x = 50;
    windowParams.y = 100;

    // 加载布局
    view = LayoutInflater.from(context).inflate(R.layout.floating_watermark, null);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    view.setLayoutParams(params);

    // 获取文本控件引用
    titleTextView = view.findViewById(R.id.watermarkTitle);
    textTextView = view.findViewById(R.id.watermarkText);
    messageTextView = view.findViewById(R.id.watermarkMessage);

    AppInfo appInfo = new AppInfo(context);
    // 获取应用版本号
    String appVersionName = appInfo.getAppVersionName();

    try {
      // 从 assets 目录中读取文件
      String base64Ciphertext =
          AssetsFileUtils.readFromAssets(context, "content/WatermarkFloatWindow");

      // 解密
      byte[] key = "Fsy".getBytes();
      String[] decryptedText = m2.m2d(context,key, base64Ciphertext).split("\n");

      StringUtil stringUtil = new StringUtil();

      // 设置预设文本
      titleTextView.setText(stringUtil.extractContentBetweenBrackets(decryptedText[0]));
      textTextView.setText(
          "v" + appVersionName + stringUtil.extractContentBetweenBrackets(decryptedText[1]));
      messageTextView.setText(stringUtil.extractContentBetweenBrackets(decryptedText[2]));
    } catch (IOException e) {
      e.printStackTrace();
      // 设置默认文本
      titleTextView.setText("Fox Craft");
      textTextView.setText("v" + appVersionName);
      messageTextView.setText("");
    }
  }

  // 单例获取方法
  public static WatermarkFloatWindow getInstance(Context context) {
    if (instance == null) {
      synchronized (WatermarkFloatWindow.class) {
        if (instance == null) {
          instance = new WatermarkFloatWindow(context.getApplicationContext());
        }
      }
    }
    return instance;
  }

  // 显示悬浮窗
  public void show() {
    try {
      if (view.getParent() == null) {
        windowManager.addView(view, windowParams);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 隐藏悬浮窗
  public void hide() {
    try {
      if (view.getParent() != null) {
        windowManager.removeView(view);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 修改标题文本
  public void setTitleText(String text) {
    if (titleTextView != null) {
      titleTextView.setText(text);
    }
  }

  // 修改版本文本
  public void setVersionText(String text) {
    if (textTextView != null) {
      textTextView.setText(text);
    }
  }

  // 修改消息文本
  public void setMessageText(String text) {
    if (messageTextView != null) {
      messageTextView.setText(text);
    }
  }
}
