package com.example.mine.Service;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.mine.R;
import com.example.mine.tool.Function;
import com.william.gradient.GradientTextView;
import java.util.ArrayList;
import java.util.List;

/** 功能显示悬浮窗管理类（单例模式） 用于显示已开启的功能列表，支持添加和删除功能 */
public class FeatureDisplayManager {
  private static volatile FeatureDisplayManager instance;

  private Context context;
  private WindowManager windowManager;
  private View featureView;
  private GradientTextView featuresTextView;
  private WindowManager.LayoutParams featureParams;
  private boolean isShowing = false;
  private List<String> featuresList = new ArrayList<>();
  private Function function;

  private FeatureDisplayManager(Context context) {
    this.context = context.getApplicationContext();
    this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
    initLayoutParams();
    initView();
    function = new Function(context);
  }

  /**
   * 获取单例实例
   *
   * @param context 上下文
   * @return 单例对象
   */
  public static FeatureDisplayManager getInstance(Context context) {
    if (instance == null) {
      synchronized (FeatureDisplayManager.class) {
        if (instance == null) {
          instance = new FeatureDisplayManager(context);
        }
      }
    }
    return instance;
  }

  /** 初始化悬浮窗布局参数 */
  private void initLayoutParams() {
    featureParams =
        new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 悬浮窗的类型
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);

    // 定位到右上角
    featureParams.gravity = Gravity.TOP | Gravity.END;
    featureParams.alpha = 0.6f;
    // 右上角留出一点边距
    featureParams.x = 30;
    featureParams.y = 30;
  }

  /** 初始化悬浮窗视图 */
  private void initView() {
    LayoutInflater inflater = LayoutInflater.from(context);
    featureView = inflater.inflate(R.layout.feature_display_overlay, null);
    featuresTextView = featureView.findViewById(R.id.featureName);
  }

  /** 更新显示的功能列表 */
  private void updateFeaturesDisplay() {
    if (featuresTextView == null) return;

    // 将列表用换行符拼接成字符串
    StringBuilder sb = new StringBuilder();
    for (String feature : featuresList) {
      sb.append(feature).append("\n");
    }

    // 移除最后一个多余的换行符
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    featuresTextView.setText(sb.toString());
    boolean functionDisplayStatus = function.getStatus("functionDisplay");
    // 如果列表为空，隐藏悬浮窗；否则显示
    if (featuresList.isEmpty()||functionDisplayStatus==false) {
      hide();
    } else {
      show();
    }
  }

  /**
   * 添加功能到显示列表
   *
   * @param feature 功能名称
   */
  public void addFeature(String feature) {
    if (feature == null || feature.isEmpty()) return;

    // 避免重复添加
    if (!featuresList.contains(feature)) {
      featuresList.add(feature);
      updateFeaturesDisplay();
    }
  }

  /**
   * 从显示列表中移除功能
   *
   * @param feature 功能名称
   */
  public void removeFeature(String feature) {
    if (feature == null || feature.isEmpty()) return;

    featuresList.remove(feature);
    updateFeaturesDisplay();
  }

  /** 清空所有功能 */
  public void clearAllFeatures() {
    featuresList.clear();
    updateFeaturesDisplay();
  }

  /** 显示悬浮窗 */
  public void show() {
    if (isShowing || featureView == null) return;

    try {
      if (featureView.getParent() == null) {
        windowManager.addView(featureView, featureParams);
      }
      featureView.setVisibility(View.VISIBLE);
      isShowing = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 隐藏悬浮窗 */
  public void hide() {
    if (!isShowing || featureView == null) return;

    try {
      if (featureView.getParent() != null) {
        windowManager.removeView(featureView);
      }
      isShowing = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 检查悬浮窗是否正在显示
   *
   * @return 是否显示
   */
  public boolean isShowing() {
    return isShowing;
  }

  /**
   * 检查功能是否已在列表中
   *
   * @param feature 功能名称
   * @return 是否存在
   */
  public boolean hasFeature(String feature) {
    return featuresList.contains(feature);
  }

  public void setGONE() {
    CardView card = featureView.findViewById(R.id.card);
    card.setVisibility(View.GONE);
  }

  public void setVISIBLE() {
    CardView card = featureView.findViewById(R.id.card);
    card.setVisibility(View.VISIBLE);
  }
}
