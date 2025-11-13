package com.example.mine.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.Adapter.*;
import com.example.mine.Item.*;
import com.example.mine.R;
import com.example.mine.Service.ShortcutKey;
import com.example.mine.tool.ShortcutKeyManager;
import com.example.mine.tool.StatusTool;
import com.example.mine.util.DexVerificationManager;
import com.example.mine.util.GetString;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ToolService extends Service {
  private static final String CHANNEL_ID = "tool_service_channel";
  private static boolean isServiceRunning = false;
  private long startTime;
  private View floatingView;
  private boolean isViewRemoved = false;
  private FixedWatermarkOverlayManager watermarkManager;
  private WeakReference<Context> weakContext;
  private long onStartTime;
  private NotificationManager notificationManager;

  public ServiceWindowIcon serviceWindowIcon;

  @Override
  public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    GetString getString = new GetString();
    String[] result =
        getString.getStringsFromKeywordWrapper(
            this, getString, UUID.randomUUID().toString().replaceAll("-", ""));
    if (!StatusTool.isVerificationSuccess() && (!DexVerificationManager.DEBUG)) return;
    // 创建通知渠道（适用于 Android 8.0 及以上版本）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              CHANNEL_ID, "Tool Service Channel", NotificationManager.IMPORTANCE_LOW);
      getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    long n = intent.getLongExtra("o", 0);
    long nn = intent.getLongExtra(".", 0);
    

    if ("STOP_SERVICE".equals(intent.getAction())) {
      stopForeground(true);
      stopSelf();
      return START_NOT_STICKY;
    }

    if (!StatusTool.isVerificationSuccess() && (!DexVerificationManager.DEBUG)) {
      stopForeground(true);
      stopSelf();
      return START_NOT_STICKY;
    }

    // 设置服务运行状态
    isServiceRunning = true;

    // 记录服务启动时间
    startTime = SystemClock.elapsedRealtime();

    onStartTime = 0;

    // 初始化快捷键对象
    ShortcutKeyManager.initShortcutKey(this);
    // 检测悬浮窗权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        Intent intent1 =
            new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
        return START_NOT_STICKY;
      }
    }
    // 创建通知渠道（Android O及以上版本需要）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              "fox_service_channel", "狐狸科技(映射服务)", NotificationManager.IMPORTANCE_DEFAULT);
      NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      if (manager != null) {
        manager.createNotificationChannel(channel);
      }
    }

    // 创建前台服务通知
    Notification notification =
        new Notification.Builder(this, "fox_service_channel")
            .setContentTitle("狐狸科技(映射服务)")
            .setContentText("服务运行中")
            .setSmallIcon(R.drawable.icon27_2x)
            .build();

    // 将服务置于前台状态
    startForeground(1, notification);

    // 后台任务
    performBackgroundTask();

    // 创建悬浮窗
    createIconFloatingWindow();
    Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();

    return START_STICKY;
  }

  private void createIconFloatingWindow() {
    serviceWindowIcon = new ServiceWindowIcon(this);
    serviceWindowIcon.showIcon();
  }

  // 后台任务
  private void performBackgroundTask() {
    // 初始化遮罩
    watermarkManager = FixedWatermarkOverlayManager.getInstance(this);

    // 设置水印内容（所有水印都将显示此内容）
    watermarkManager.setWatermarkText("");

    // 设置水印颜色（半透明灰色）
    watermarkManager.setWatermarkColor(Color.parseColor("#aaffb1c8"));

    // 设置水印文字大小
    watermarkManager.setWatermarkTextSize(20);

    // 设置水印间距（现在会生效）
    watermarkManager.setWatermarkSpacing(150, 100);

    // 设置旋转角度（45度）
    watermarkManager.setRotationDegree(-25);

    // 显示遮罩
    watermarkManager.show();
    Context context = this;

    Timer timer = new Timer();
    TimerTask task =
        new TimerTask() {
          @Override
          public void run() {
            GetString getString = new GetString();
            String[] result = getString.getStringsFromKeywordWrapper(context, getString, "c");
          }
        };

    // Schedule the task to run every 5 seconds
    timer.scheduleAtFixedRate(task, 0, 5000);

    new Thread(
            () -> {
              while (isServiceRunning) {
                try {
                  Thread.sleep(1000); // 每秒更新一次通知
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                updateNotification();
              }
            })
        .start();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // 停止前台服务并移除通知
    stopForeground(true);
    isServiceRunning = false; // 更新服务运行状态
    ShortcutKeyManager.killShortcutKey();
    // 关闭遮罩
    if(watermarkManager!=null) watermarkManager.hide();
    // 移除悬浮窗
    if(serviceWindowIcon!=null) serviceWindowIcon.hideIcon();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static boolean isServiceRunning() {
    return isServiceRunning;
  }

  private void updateNotification() {
    long elapsedTime = SystemClock.elapsedRealtime() - onStartTime; // 使用 onStartTime
    long minutes = elapsedTime / 60000;
    long seconds = (elapsedTime % 60000) / 1000;

    String contentText = String.format("已启动%02d分%02d秒", minutes, seconds);

    Notification notification =
        new NotificationCompat.Builder(this, "fox_service_channel")
            .setContentTitle("狐狸科技(映射服务)")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.icon27_2x)
            .build();

    if (notificationManager != null) {
      notificationManager.notify(1, notification);
    }
  }
}
