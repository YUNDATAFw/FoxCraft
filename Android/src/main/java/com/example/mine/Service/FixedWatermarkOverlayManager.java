package com.example.mine.Service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

/** 修复了间距设置和换行问题的水印遮罩悬浮窗管理类 */
public class FixedWatermarkOverlayManager {
  private static volatile FixedWatermarkOverlayManager instance;

  private Context context;
  private WindowManager windowManager;
  private WatermarkView watermarkView;
  private WindowManager.LayoutParams overlayParams;
  private boolean isShowing = false;

  // 遮罩颜色默认值（透明）
  private int overlayColor = Color.argb(0, 0, 0, 0);
  // 水印默认属性
  private String watermarkText = "水印";
  private int watermarkColor = Color.argb(30, 0, 0, 0); // 半透明黑色
  private int watermarkSizeSp = 16; // 16sp
  private int horizontalSpacingDp = 150; // 水平间距(dp)
  private int verticalSpacingDp = 80; // 垂直间距(dp)
  private int rotationDegree = -45; // 旋转角度

  private FixedWatermarkOverlayManager(Context context) {
    this.context = context.getApplicationContext();
    this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
    initLayoutParams();
    initWatermarkView();
  }

  /** 获取单例实例 */
  public static FixedWatermarkOverlayManager getInstance(Context context) {
    if (instance == null) {
      synchronized (FixedWatermarkOverlayManager.class) {
        if (instance == null) {
          instance = new FixedWatermarkOverlayManager(context);
        }
      }
    }
    return instance;
  }

  /** 初始化悬浮窗布局参数 */
  private void initLayoutParams() {
    overlayParams =
    new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 悬浮窗的类型
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT // 透明度设置
      );
  }

  /** 初始化水印视图 */
  private void initWatermarkView() {
    watermarkView = new WatermarkView(context);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    watermarkView.setLayoutParams(params);
    watermarkView.setOverlayColor(overlayColor);
  }

  /** 显示水印 */
  public void show() {
    if (isShowing || watermarkView == null) return;

    try {
      if (watermarkView.getParent() == null) {
        windowManager.addView(watermarkView, overlayParams);
      }
      watermarkView.setVisibility(View.VISIBLE);
      isShowing = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 关闭水印 */
  public void hide() {
    if (!isShowing || watermarkView == null) return;

    try {
      if (watermarkView.getParent() != null) {
        windowManager.removeView(watermarkView);
      }
      isShowing = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 切换显示状态 */
  public void toggle() {
    if (isShowing) {
      hide();
    } else {
      show();
    }
  }

  /**
   * 设置遮罩颜色
   *
   * @param color 颜色值，包含透明度
   */
  public void setOverlayColor(int color) {
    overlayColor = color;
    if (watermarkView != null) {
      watermarkView.setOverlayColor(overlayColor);
    }
  }

  /**
   * 通过ARGB值修改遮罩颜色
   *
   * @param alpha 透明度 (0-255)
   * @param red 红色分量 (0-255)
   * @param green 绿色分量 (0-255)
   * @param blue 蓝色分量 (0-255)
   */
  public void setFilterColor(int alpha, int red, int green, int blue) {
    if (watermarkView != null) {
      watermarkView.setOverlayColor(Color.argb(alpha, red, green, blue));
    }
  }

  /**
   * 设置水印颜色
   *
   * @param color 颜色值，包含透明度
   */
  public void setWatermarkColor(int color) {
    watermarkColor = color;
    if (watermarkView != null) {
      watermarkView.invalidate();
    }
  }

  /**
   * 设置水印文字大小
   *
   * @param sizeSp 文字大小，单位sp
   */
  public void setWatermarkTextSize(int sizeSp) {
    watermarkSizeSp = sizeSp;
    if (watermarkView != null) {
      watermarkView.invalidate();
    }
  }

  /**
   * 检查是否正在显示
   *
   * @return 是否显示
   */
  public boolean isShowing() {
    return isShowing;
  }

  /**
   * 设置水印间距 - 修复版
   *
   * @param horizontalDp 水平间距，单位dp
   * @param verticalDp 垂直间距，单位dp
   */
  public void setWatermarkSpacing(int horizontalDp, int verticalDp) {
    // 保存新的间距值
    this.horizontalSpacingDp = horizontalDp;
    this.verticalSpacingDp = verticalDp;

    // 立即更新视图的间距值并重新绘制
    if (watermarkView != null) {
      watermarkView.updateSpacing();
      watermarkView.invalidate();
    }
  }

  /**
   * 设置水印文本内容（支持\n换行）
   *
   * @param text 水印文本
   */
  public void setWatermarkText(String text) {
    if (text != null) {
      watermarkText = text;
      if (watermarkView != null) {
        watermarkView.invalidate();
      }
    }
  }

  /**
   * 设置水印旋转角度
   *
   * @param degree 旋转角度，正值顺时针，负值逆时针
   */
  public void setRotationDegree(int degree) {
    rotationDegree = degree;
    if (watermarkView != null) {
      watermarkView.invalidate();
    }
  }

  /** 自定义视图，修复了间距和换行问题 */
  private class WatermarkView extends FrameLayout {
    private TextPaint watermarkPaint;
    private int currentOverlayColor;
    // 像素间距（实时更新）
    private int horizontalSpacingPx;
    private int verticalSpacingPx;

    public WatermarkView(Context context) {
      super(context);
      initPaint();
      updateSpacing(); // 初始化间距
      setWillNotDraw(false);
    }

    // 新增：更新间距方法，供外部调用
    public void updateSpacing() {
      float density = getResources().getDisplayMetrics().density;
      horizontalSpacingPx = (int) (horizontalSpacingDp * density);
      verticalSpacingPx = (int) (verticalSpacingDp * density);
    }

    private void initPaint() {
      watermarkPaint = new TextPaint();
      watermarkPaint.setColor(watermarkColor);
      watermarkPaint.setTextSize(
          TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_SP, watermarkSizeSp, getResources().getDisplayMetrics()));
      watermarkPaint.setAntiAlias(true);
    }

    public void setOverlayColor(int color) {
      currentOverlayColor = color;
      invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      canvas.drawColor(currentOverlayColor);

      // 更新画笔属性
      watermarkPaint.setColor(watermarkColor);
      watermarkPaint.setTextSize(
          TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_SP, watermarkSizeSp, getResources().getDisplayMetrics()));

      int width = getWidth();
      int height = getHeight();

      if (width <= 0 || height <= 0 || watermarkText.isEmpty()) {
        return;
      }

      // 计算文本尺寸（支持多行）
      int textWidth = 200; // 限制文本宽度，让长文本自动换行
      StaticLayout staticLayout =
          new StaticLayout(
              watermarkText,
              watermarkPaint,
              textWidth,
              Layout.Alignment.ALIGN_NORMAL,
              1.0f,
              0.0f,
              false);

      // 获取文本实际占用的宽高
      int textHeight = staticLayout.getHeight();

      // 计算起始位置
      int startX = -textWidth / 2;
      int startY = textHeight / 2;

      // 根据当前有效间距计算行列数
      int columns = (width + horizontalSpacingPx + textWidth) / horizontalSpacingPx + 1;
      int rows = (height + verticalSpacingPx + textHeight) / verticalSpacingPx + 1;

      // 绘制网格状水印
      for (int i = 0; i < columns; i++) {
        for (int j = 0; j < rows; j++) {
          int x = startX + i * horizontalSpacingPx;
          int y = startY + j * verticalSpacingPx;

          canvas.save();
          canvas.translate(x, y);
          canvas.rotate(rotationDegree);

          // 绘制多行文本
          staticLayout.draw(canvas);

          canvas.restore();
        }
      }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      invalidate();
    }
  }
}
