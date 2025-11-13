package com.example.mine.layout.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mine.layout.TogaActivity;
import com.example.mine.tool.StatusTool;
import com.example.mine.util.GetString;
import com.example.mine.util.m2;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import com.example.mine.Adapter.NotificationAdapter;
import com.example.mine.CustomInputMethodService;
import com.example.mine.Item.Notification;
import com.example.mine.R;
import com.example.mine.Service.ToolService;
import com.example.mine.tool.ShortcutKeyManager;
import com.example.mine.util.DexVerificationManager;
import com.example.mine.util.HealthServiceChecker;
import com.example.mine.util.KeyboardServiceChecker;
import com.example.mine.util.SecureUrlDecoder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultCallback;
import com.google.android.material.textview.MaterialTextView;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {
  private static final String TAG = "HomeFragment";
  private static final int REQUEST_OVERLAY_PERMISSION_CODE = 1;
  private ExtendedFloatingActionButton openServiceFab;
  private MaterialSwitch overlayPermissionButton;
  private MaterialSwitch checkKeyboardEnabledButton;
  private MaterialCardView keyboardCheckedCard;
  private RecyclerView recyclerView;
  private NotificationAdapter adapter;
  private List<Notification> notifications;


  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    // Find the ExtendedFloatingActionButton by ID
    openServiceFab = view.findViewById(R.id.open_service_fab);
    overlayPermissionButton = view.findViewById(R.id.overlay_permission_button);
    checkKeyboardEnabledButton = view.findViewById(R.id.check_keyboard_enabled_button);
    keyboardCheckedCard = view.findViewById(R.id.keyboard_checked_card);

    recyclerView = view.findViewById(R.id.recyclerView);

    AlphaAnimation alphaAniShow = new AlphaAnimation(0, 1); // 透明度从0%到100%
    alphaAniShow.setDuration(2500); // 设置动画持续时间为2500毫秒
    alphaAniShow.setStartOffset(1000);
    view.startAnimation(alphaAniShow); // 将动画应用到控件上
    
    // Update the service status
    updateServiceStatus();

    // Set the click listener for the floating action button
    openServiceFab.setOnClickListener(
        v -> {
          // if (StatusTool.isVerificationSuccess()) controlServiceAndRefreshStatus();
          controlServiceAndRefreshStatus();
        });

    
    MaterialTextView bulletin_content = view.findViewById(R.id.bulletin_content);
    Button bulletinCloseButton = view.findViewById(R.id.bulletinCloseButton);
    MaterialCardView bulletin_card = view.findViewById(R.id.bulletin_card);

    new Handler(Looper.getMainLooper())
        .postDelayed(
            new Runnable() {
              @Override
              public void run() {
                if (StatusTool.isVerificationSuccess() || DexVerificationManager.DEBUG) {
                  openServiceFab.setEnabled(true);
                } else {
                  openServiceFab.setEnabled(false);
                }
              }
            },
            1000); // 5000毫秒 = 5秒

    // 获取通知
    HealthServiceChecker.checkService(
        getActivity(),
        (result, alerts) -> {
          getActivity()
              .runOnUiThread(
                  () -> {
                    // 在这里处理返回的结果
                    bulletin_content.setText(result);
                    try {
                      recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                      notifications = new ArrayList<>();
                      for (int i = 0; i < alerts.length(); i++) {
                        JSONObject jsonObject = alerts.getJSONObject(i);
                        int level = jsonObject.getInt("level"); // 通知等级
                        String title = jsonObject.getString("title"); // 通知标题
                        String message = jsonObject.getString("message"); // 通知内容
                        JSONObject versionRange = jsonObject.getJSONObject("versionRange");
                        String minVersion = versionRange.getString("min"); // 最低适用版本
                        String maxVersion = versionRange.getString("max"); // 最高适用版本
                        String levelMsg = "预警" + level + "级";
                        switch (level) {
                          case 0:
                            levelMsg = "公告";
                            break;
                          case 1:
                            levelMsg = "低危";
                            break;
                          case 2:
                            levelMsg = "中危";
                            break;
                          case 3:
                            levelMsg = "高危";
                            break;
                          case 4:
                            levelMsg = "恶劣";
                            break;
                        }
                        String levelPrefix = (level == -1) ? "" : "「" + levelMsg + "」";
                        notifications.add(
                            new Notification(
                                levelPrefix + title,
                                message + "\n适用版本［v" + minVersion + " ― v" + maxVersion + "］"));
                      }
                      adapter = new NotificationAdapter(notifications);
                      recyclerView.setAdapter(adapter);
                    } catch (JSONException e) {
                    }
                  });
        });
    // 读取数据
    try {
      FileInputStream fis = getActivity().openFileInput("settings.json");
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      JSONObject settings = new JSONObject(sb.toString());
      String title = settings.getString("title");
      if ("false".equals(title)) {
        bulletin_card.setVisibility(View.GONE);
      }
      fis.close();
    } catch (Exception e) {
    }

    bulletinCloseButton.setOnClickListener(
        v -> {
          // 保存数据
          JSONObject settings = new JSONObject();
          try {
            settings.put("title", "false");
            FileOutputStream fos =
                getActivity().openFileOutput("settings.json", Context.MODE_PRIVATE);
            fos.write(settings.toString().getBytes());
            fos.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          bulletin_card.setVisibility(View.GONE);
        });

    // 设置悬浮窗权限按钮的状态和监听器
    setupOverlayPermissionButton();

    // 设置键盘服务按钮的状态和监听器
    setupKeyboardServiceButton();

    setupKeyboardCheckedButton();
    return view;
  }

  private void setupKeyboardCheckedButton() {
    keyboardCheckedCard.setOnClickListener(
        v -> {
          InputMethodManager inputMethodManager =
              (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.showInputMethodPicker();
        });
  }

  private void setupOverlayPermissionButton() {
    // 检查悬浮窗权限是否已授予
    boolean hasOverlayPermission = checkOverlayPermission();
    // 根据权限状态设置 MaterialSwitch 的状态
    overlayPermissionButton.setChecked(hasOverlayPermission);

    // 设置 MaterialSwitch 的状态改变监听器
    overlayPermissionButton.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            if (!checkOverlayPermission()) {
              // 如果用户将开关设置为开启，检查权限是否已授予
              if (!checkOverlayPermission()) {
                // 如果权限未授予，提示用户并跳转到设置页面
                new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("授予权限")
                    .setMessage(
                        "运行 “"
                            + getAppName()
                            + "” 此应用中的服务需要授予 “显示在其他应用上层” 权限才可启用，如不授予将会禁用相关服务，直到权限被启用，您可以点击下方授权按钮跳转授权页面授予此应用权限。")
                    .setPositiveButton("授权", (d, w) -> requestOverlayPermission())
                    .setNegativeButton(
                        "取消",
                        (d, w) -> {
                          d.dismiss();
                          overlayPermissionButton.setChecked(false);
                        })
                    .show();
              }
            }
          } else {
            if (checkOverlayPermission()) {
              new MaterialAlertDialogBuilder(getActivity())
                  .setTitle("注销权限")
                  .setMessage(
                      "如果您希望注销 “显示在其他应用上层” 权限，您可以点击下方注销按钮跳转权限管理页面，找到 “"
                          + getAppName()
                          + "” 取消勾选即可注销此应用的权限，此应用将不在拥有该权限，相关服务也无法启动与使用。")
                  .setPositiveButton(
                      "注销",
                      (d, w) -> { // 如果用户将开关设置为关闭，引导用户关闭悬浮窗权限
                        guideUserToDisableOverlayPermission();
                      })
                  .setNegativeButton(
                      "取消",
                      (d, w) -> {
                        d.dismiss();
                        overlayPermissionButton.setChecked(true);
                      })
                  .show();
            }
          }
        });
  }

  private void setupKeyboardServiceButton() {
    // 检查键盘服务是否已勾选
    boolean isKeyboardEnabled =
        KeyboardServiceChecker.isKeyboardServiceEnabled(
            getContext(), "com.example.mine.CustomInputMethodService");
    // 根据键盘服务状态设置 MaterialSwitch 的状态
    checkKeyboardEnabledButton.setChecked(isKeyboardEnabled);

    // 设置 MaterialSwitch 的状态改变监听器
    checkKeyboardEnabledButton.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            if (!KeyboardServiceChecker.isKeyboardServiceEnabled(
                getContext(), "com.example.mine.CustomInputMethodService")) {
              // 如果用户将开关设置为开启，检查键盘服务是否已勾选
              if (!KeyboardServiceChecker.isKeyboardServiceEnabled(
                  getContext(), "com.example.mine.CustomInputMethodService")) {
                // 如果键盘服务未勾选，提示用户并跳转到设置页面
                new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("授予权限")
                    .setMessage(
                        "运行 “"
                            + getAppName()
                            + "” 此应用中的服务需要授予 “键盘服务” 权限才可启用，如不授予将会禁用相关服务，直到权限被启用，您可以点击下方授权按钮跳转授权页面授予此应用权限。")
                    .setPositiveButton(
                        "授权",
                        (d, w) -> {
                          Toast.makeText(getContext(), "请勾选键盘服务", Toast.LENGTH_SHORT).show();
                          showInputMethodSettingsDialog();
                        })
                    .setNegativeButton(
                        "取消",
                        (d, w) -> {
                          d.dismiss();
                          checkKeyboardEnabledButton.setChecked(false);
                        })
                    .show();
              }
            }
          } else {
            if (KeyboardServiceChecker.isKeyboardServiceEnabled(
                getContext(), "com.example.mine.CustomInputMethodService")) {
              // 如果用户将开关设置为关闭，引导用户取消键盘服务
              new MaterialAlertDialogBuilder(getActivity())
                  .setTitle("注销权限")
                  .setMessage(
                      "如果您希望注销 “键盘服务” 权限，您可以点击下方注销按钮跳转权限管理页面，找到 “"
                          + getAppName()
                          + "” 取消勾选即可注销此应用的权限，此应用将不在拥有该权限，相关服务也无法启动与使用。")
                  .setPositiveButton(
                      "注销",
                      (d, w) -> {
                        guideUserToDisableKeyboardService();
                      })
                  .setNegativeButton(
                      "取消",
                      (d, w) -> {
                        d.dismiss();
                        checkKeyboardEnabledButton.setChecked(true);
                      })
                  .show();
            }
          }
        });
  }

  // 检查悬浮窗权限的方法
  private boolean checkOverlayPermission() {
    return Settings.canDrawOverlays(getContext());
  }

  // 请求悬浮窗权限的方法
  private void requestOverlayPermission() {
    Intent intent =
        new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getContext().getPackageName()));
    startActivity(intent);
  }

  // 引导用户关闭悬浮窗权限的方法
  private void guideUserToDisableOverlayPermission() {
    Toast.makeText(getContext(), "请前往设置关闭悬浮窗权限", Toast.LENGTH_SHORT).show();
    Intent intent =
        new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getContext().getPackageName()));
    startActivity(intent);
  }

  // 引导用户取消键盘服务的方法
  private void guideUserToDisableKeyboardService() {
    Toast.makeText(getContext(), "请前往设置取消键盘服务", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
    startActivity(intent);
  }

  // 显示输入法设置对话框的方法
  private void showInputMethodSettingsDialog() {
    Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
    startActivity(intent);
  }

  private void updateServiceStatus() {
    if (ToolService.isServiceRunning()) {
      openServiceFab.setText("停止服务");
      openServiceFab.setIconResource(R.drawable.leaf);
    } else {
      openServiceFab.setText("开始服务");
      openServiceFab.setIconResource(R.drawable.leaf_off);
    }
  }

  // 获取应用名称
  private String getAppName() {
    if (getContext() == null) {
      return "未知";
    }
    PackageManager packageManager = getContext().getPackageManager();
    try {
      ApplicationInfo applicationInfo =
          packageManager.getApplicationInfo(getContext().getPackageName(), 0);
      return packageManager.getApplicationLabel(applicationInfo).toString();
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return "未知";
    }
  }

  // 回调事件
  @Override
  public void onResume() {
    super.onResume();
    // 检查悬浮窗权限状态并更新开关
    boolean hasOverlayPermission = checkOverlayPermission();
    overlayPermissionButton.setChecked(hasOverlayPermission);

    // 检查键盘服务状态并更新开关
    boolean isKeyboardEnabled =
        KeyboardServiceChecker.isKeyboardServiceEnabled(
            getContext(), "com.example.mine.CustomInputMethodService");
    checkKeyboardEnabledButton.setChecked(isKeyboardEnabled);
  }

  // 控制服务的启动和停止，并更新服务状态
  private void controlServiceAndRefreshStatus() {
    // 检查服务是否正在运行
    if (ToolService.isServiceRunning()) {
      // 停止服务
      Intent stopIntent = new Intent(getContext(), ToolService.class);
      stopIntent.setAction("STOP_SERVICE");
      getContext().startService(stopIntent);
      // 创建并启动子线程
      new Thread(
              () -> {
                // 让线程等待 300 毫秒
                try {
                  Thread.sleep(200);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }

                // 在主线程中更新 UI
                getActivity().runOnUiThread(this::updateServiceStatus);
              })
          .start();
    } else {
      // 启动服务前检查权限
      if (checkOverlayPermission()) {
        if (KeyboardServiceChecker.isKeyboardServiceEnabled(
            getContext(), "com.example.mine.CustomInputMethodService")) {
          CustomInputMethodService test = new CustomInputMethodService();
          if (test.sendTest()) {
            // 启动服务
            GetString getString = new GetString();
            String[] result =
                getString.getStringsFromKeywordWrapper(getActivity(), getString, "MIKU");
            if (result != null) {
                // 创建Intent实例
                Intent serviceIntent = new Intent(getActivity(), ToolService.class);
                
                // 调用putExtra方法添加额外数据
                serviceIntent.putExtra(".", Instant.now().toEpochMilli());
                serviceIntent.putExtra("o", 0);
                
                // 调用startService方法启动服务
                getActivity().startService(serviceIntent);

            } else {
              Toast.makeText(getActivity(), "无法连接到Tool库", Toast.LENGTH_SHORT).show();
            }
            // 创建并启动子线程
            new Thread(
                    () -> {
                      // 让线程等待 300 毫秒
                      try {
                        Thread.sleep(200);
                      } catch (InterruptedException e) {
                        e.printStackTrace();
                      }

                      // 在主线程中更新 UI
                      getActivity().runOnUiThread(this::updateServiceStatus);
                    })
                .start();
          } else {
            new MaterialAlertDialogBuilder(getActivity())
                .setTitle("异常")
                .setMessage(
                    "本次启动被阻止\n原因：当前使用的输入法非本应用提供\n解决方法：请点击下方「切换」按钮将键盘切换至 “"
                        + getAppName()
                        + "” 后请重新开启服务。\n如果您希望稍后切换可点击下方「忽略」按钮(有几率崩溃)")
                .setPositiveButton(
                    "确定",
                    (d, w) -> {
                      d.dismiss();
                    })
                .setNeutralButton(
                    "切换",
                    (d, w) -> {
                      // 获取 InputMethodManager 实例
                      InputMethodManager inputMethodManager =
                          (InputMethodManager)
                              requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                      inputMethodManager.showInputMethodPicker();
                    })
                .setNegativeButton(
                    "忽略",
                    (d, w) -> {
                      d.dismiss();
                      // 启动服务
                      GetString getString = new GetString();
                      String[] result =
                          getString.getStringsFromKeywordWrapper(getActivity(), getString, "MIKU");
                      if (result != null) {
                        
                        // 创建Intent实例
                        Intent serviceIntent = new Intent(getActivity(), ToolService.class);
                        
                        // 调用putExtra方法添加额外数据
                        serviceIntent.putExtra(".", Instant.now().toEpochMilli());
                        serviceIntent.putExtra("o", 0);
                        
                        // 调用startService方法启动服务
                        getActivity().startService(serviceIntent);
                        
                      } else {
                        Toast.makeText(getActivity(), "无法连接到Tool库", Toast.LENGTH_SHORT).show();
                      }
                      // 创建并启动子线程
                      new Thread(
                              () -> {
                                // 让线程等待 200 毫秒
                                try {
                                  Thread.sleep(200);
                                } catch (InterruptedException e) {
                                  e.printStackTrace();
                                }

                                // 在主线程中更新 UI
                                getActivity().runOnUiThread(this::updateServiceStatus);
                              })
                          .start();
                    })
                .show();
          }
        } else {
          new MaterialAlertDialogBuilder(getActivity())
              .setTitle("异常")
              .setMessage("本次启动被阻止\n原因：请授予此应用 “键盘服务” 权限后再尝试启用服务")
              .setPositiveButton(
                  "确定",
                  (d, w) -> {
                    d.dismiss();
                  })
              .show();
        }
      } else {
        new MaterialAlertDialogBuilder(getActivity())
            .setTitle("异常")
            .setMessage("本次启动被阻止\n原因：请授予此应用 “显示在其他应用上层” 权限后再尝试启用服务")
            .setPositiveButton(
                "确定",
                (d, w) -> {
                  d.dismiss();
                })
            .show();
      }
    }
  }

  /**
   * 判断当前激活的输入法是否为本应用的输入法
   *
   * @param context 应用上下文
   * @return 如果当前输入法是本应用的输入法，返回 true；否则返回 false
   */
  public static boolean isCurrentInputMethodOfThisApp() {
    CustomInputMethodService test = new CustomInputMethodService();
    return test.sendTest();
  }

  public int ran(Integer n1, Integer n2, Integer n3) {
    int min = 100000; // 6位数的最小值
    int max = (n1 * 10000) + (n2 * 100) + n3; // 6位数的最大值
    int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
    return randomNum;
  }
}
