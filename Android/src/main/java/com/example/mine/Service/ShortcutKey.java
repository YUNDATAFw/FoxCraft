package com.example.mine.Service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.mine.R;

public class ShortcutKey extends LinearLayout {

    public enum Mode {
        BUTTON, // 按钮模式（只能点击）
        SWITCH  // 开关模式（有开和关两种状态）
    }

    // 按钮模式点击回调接口
    public interface OnClickListener {
        void onClick();
    }

    // 开关模式状态变化回调接口
    public interface OnSwitchListener {
        void onOpen();  // 开启状态回调
        void onClose(); // 关闭状态回调
    }

    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean isDragging = false;
    private boolean enable = false; // 开关状态
    private View view;
    private String title;
    private Mode mode; // 模式
    private OnClickListener clickListener;
    private OnSwitchListener switchListener;

    // 按钮模式构造方法
    public ShortcutKey(Context context, String title, Mode mode, OnClickListener clickListener) {
        this(context, null, title, mode, clickListener, null);
    }

    // 开关模式构造方法 - 支持Lambda表达式
    public ShortcutKey(Context context, String title, Mode mode, 
                      Runnable onOpen, Runnable onClose) {
        this(context, null, title, mode, null, 
             new OnSwitchListener() {
                 @Override
                 public void onOpen() {
                     onOpen.run();
                 }
                 @Override
                 public void onClose() {
                     onClose.run();
                 }
             });
    }

    // 开关模式构造方法 - 保留接口方式
    public ShortcutKey(Context context, String title, Mode mode, OnSwitchListener switchListener) {
        this(context, null, title, mode, null, switchListener);
    }

    public ShortcutKey(Context context, AttributeSet attrs, String title, Mode mode,
                      OnClickListener clickListener, OnSwitchListener switchListener) {
        this(context, attrs, 0, title, mode, clickListener, switchListener);
    }

    public ShortcutKey(Context context, AttributeSet attrs, int defStyleAttr, String title,
                      Mode mode, OnClickListener clickListener, OnSwitchListener switchListener) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.title = title;
        this.mode = mode;
        this.clickListener = clickListener;
        this.switchListener = switchListener;
        initFloatingWindow();
    }

    private void initFloatingWindow() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        view = inflate(context, R.layout.shortcut_key_window, this);
        LinearLayout card = view.findViewById(R.id.card);
        TextView text = view.findViewById(R.id.text);
        text.setText(title);

        // 初始化样式
        updateUI();

        // 设置点击事件
        card.setOnClickListener(v -> {
            if (mode == Mode.BUTTON) {
                // 按钮模式：执行点击回调
                if (clickListener != null) {
                    clickListener.onClick();
                }
            } else {
                // 开关模式：切换状态并执行对应回调
                toggle();
            }
        });

        // 触摸拖拽逻辑
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    isDragging = false;
                    return false;

                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) (event.getRawX() - initialTouchX);
                    int deltaY = (int) (event.getRawY() - initialTouchY);
                    if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                        isDragging = true;
                        params.x = initialX + deltaX;
                        params.y = initialY + deltaY;
                        windowManager.updateViewLayout(ShortcutKey.this, params);
                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                    boolean result = isDragging;
                    isDragging = false;
                    return result;
            }
            return false;
        });
    }

    private void updateUI() {
        LinearLayout card = view.findViewById(R.id.card);
        TextView text = view.findViewById(R.id.text);
        
        if (mode == Mode.SWITCH) {
            // 开关模式根据状态更新样式
            if (enable) {
                card.setBackgroundColor(Color.parseColor("#ddffffff"));
                text.setTextColor(Color.parseColor("#dd000000"));
            } else {
                card.setBackgroundColor(Color.parseColor("#dd000000"));
                text.setTextColor(Color.parseColor("#ddffffff"));
            }
        } else {
            // 按钮模式固定样式
            card.setBackgroundColor(Color.parseColor("#dd000000"));
            text.setTextColor(Color.parseColor("#ddffffff"));
        }
    }

    // 切换开关状态
    public void toggle() {
        if (mode == Mode.SWITCH) {
            enable = !enable;
            updateUI();
            // 触发对应状态的回调
            if (switchListener != null) {
                if (enable) {
                    switchListener.onOpen();
                } else {
                    switchListener.onClose();
                }
            }
        }
    }

    public void show() {
    if(getParent() == null) {
        windowManager.addView(this, params);
      }
    }

    public void hide() {
        // 如果是开关模式且当前是开启状态，先执行关闭逻辑
        if (mode == Mode.SWITCH && enable && switchListener != null) {
            switchListener.onClose();
            enable = false; // 确保状态同步
            updateUI();
        }
        
        if (isAttachedToWindow() && getWindowToken() != null) {
            windowManager.removeView(this);
        }
    }

    // 获取当前开关状态
    public boolean isEnabled() {
        return enable;
    }
}
