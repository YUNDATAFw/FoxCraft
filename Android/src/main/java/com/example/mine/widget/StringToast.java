package com.example.mine.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.example.mine.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class StringToast {

    /* ---------------- 枚举定义 ---------------- */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DURATION_SHORT, DURATION_LONG})
    public @interface Duration {}
    public static final int DURATION_SHORT = 2000; // 2秒
    public static final int DURATION_LONG = 3500; // 3.5秒

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POSITION_TOP, POSITION_BOTTOM, POSITION_LEFT, POSITION_RIGHT, POSITION_CENTER})
    public @interface Position {}
    public static final int POSITION_TOP = 0;
    public static final int POSITION_BOTTOM = 1;
    public static final int POSITION_LEFT = 2;
    public static final int POSITION_RIGHT = 3;
    public static final int POSITION_CENTER = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_INFO, TYPE_SUCCESS, TYPE_WARNING, TYPE_ERROR})
    public @interface Type {}
    public static final int TYPE_INFO = 0;
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_WARNING = 2;
    public static final int TYPE_ERROR = 3;

    /* ---------------- 单例 ---------------- */
    private static volatile StringToast instance;
    private final Context appCtx;
    private final WindowManager windowManager;
    private View toastView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private StringToast(Context ctx) {
        appCtx = ctx.getApplicationContext();
        windowManager = (WindowManager) appCtx.getSystemService(Context.WINDOW_SERVICE);
    }

    public static StringToast getInstance(Context ctx) {
        if (instance == null) {
            synchronized (StringToast.class) {
                if (instance == null) instance = new StringToast(ctx);
            }
        }
        return instance;
    }

    /* ---------------- 主入口 ---------------- */
    public void show(String message) {
        show(message, DURATION_SHORT, POSITION_BOTTOM, TYPE_INFO);
    }

    public void show(String message,
                     @Duration int duration,
                     @Position int position,
                     @Type int type) {
        if (message == null || message.isEmpty()) return;
        if (message.length() > 128) message = message.substring(0, 128);

        if (hasSystemAlertWindowPermission(appCtx)) {
            showFloatingToast(message, duration, position, type);
        } else {
            showNativeToast(message, duration, type);
        }
    }

    /* ---------------- 悬浮窗显示 ---------------- */
    private void showFloatingToast(String message,
                                   @Duration int duration,
                                   @Position int position,
                                   @Type int type) {
        if (toastView != null) {
            windowManager.removeView(toastView);
        }

        toastView = LayoutInflater.from(appCtx).inflate(R.layout.string_toast_layout, null);
        ImageView icon = toastView.findViewById(R.id.toast_icon);
        TextView text = toastView.findViewById(R.id.toast_message);
        LinearLayout bgView = toastView.findViewById(R.id.bg);
        text.setText(message);

        // 根据 type 设置样式
        switch (type) {
            case TYPE_SUCCESS:
                icon.setImageResource(R.drawable.ic_toast_success);
                bgView.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_toast_warning);
                icon.setColorFilter(Color.parseColor("#FE6700"), PorterDuff.Mode.SRC_IN);
                text.setTextColor(Color.parseColor("#FE6700"));
                bgView.setBackgroundColor(Color.parseColor("#FFB360"));
                break;
            case TYPE_ERROR:
                icon.setImageResource(R.drawable.ic_toast_error);
                icon.setColorFilter(Color.parseColor("#800000"), PorterDuff.Mode.SRC_IN);
                text.setTextColor(Color.parseColor("#800000"));
                bgView.setBackgroundColor(Color.parseColor("#FF6666"));
                break;
            default: // TYPE_INFO
                icon.setImageResource(R.drawable.ic_toast_info);
                bgView.setBackgroundColor(Color.parseColor("#000000"));
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.graphics.PixelFormat.TRANSLUCENT);

        params.gravity = convertGravity(position);
        params.y = getOffset(position);

        windowManager.addView(toastView, params);

        // 使用 Handler 控制实际显示时长
        handler.postDelayed(() -> {
            if (toastView != null) {
                windowManager.removeView(toastView);
                toastView = null;
            }
        }, duration);
    }

    /* ---------------- 原生 Toast 显示 ---------------- */
    private void showNativeToast(String message,
                                 @Duration int duration,
                                 @Type int type) {
        View toastView = LayoutInflater.from(appCtx).inflate(R.layout.string_toast_layout, null);
        ImageView icon = toastView.findViewById(R.id.toast_icon);
        TextView text = toastView.findViewById(R.id.toast_message);
        LinearLayout bgView = toastView.findViewById(R.id.bg);
        text.setText(message);

        // 根据 type 设置样式
        switch (type) {
            case TYPE_SUCCESS:
                icon.setImageResource(R.drawable.ic_toast_success);
                bgView.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_toast_warning);
                icon.setColorFilter(Color.parseColor("#FE6700"), PorterDuff.Mode.SRC_IN);
                text.setTextColor(Color.parseColor("#FE6700"));
                bgView.setBackgroundColor(Color.parseColor("#FFB360"));
                break;
            case TYPE_ERROR:
                icon.setImageResource(R.drawable.ic_toast_error);
                icon.setColorFilter(Color.parseColor("#800000"), PorterDuff.Mode.SRC_IN);
                text.setTextColor(Color.parseColor("#800000"));
                bgView.setBackgroundColor(Color.parseColor("#FF6666"));
                break;
            default: // TYPE_INFO
                icon.setImageResource(R.drawable.ic_toast_info);
                bgView.setBackgroundColor(Color.parseColor("#000000"));
        }

        Toast toast = new Toast(appCtx);
        toast.setView(toastView);
        toast.setDuration(duration == DURATION_SHORT ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.show();
    }

    /* ---------------- 工具 ---------------- */
    private int convertGravity(@Position int pos) {
        switch (pos) {
            case POSITION_TOP:    return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case POSITION_LEFT:   return Gravity.START | Gravity.CENTER_VERTICAL;
            case POSITION_RIGHT:  return Gravity.END | Gravity.CENTER_VERTICAL;
            case POSITION_CENTER: return Gravity.CENTER;
            default:              return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
    }

    private int getOffset(@Position int pos) {
        // 距离边缘 64 dp
        return (int) (appCtx.getResources().getDisplayMetrics().density * 64);
    }

    private boolean hasSystemAlertWindowPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public void release() {
        if (toastView != null) {
            windowManager.removeView(toastView);
            toastView = null;
        }
        handler.removeCallbacksAndMessages(null);
        instance = null;
    }
}
