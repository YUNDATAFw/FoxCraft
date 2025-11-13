package com.example.mine.layout.Fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.mine.*;
import com.example.mine.layout.HelpActivity;
import com.example.mine.layout.SettingActivity;
import com.example.mine.util.UpdateChecker;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {
  private String currentVersion = "1.0.0";

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_settings, container, false);

    Button loginButton = view.findViewById(R.id.loginButton);
    Button registerButton = view.findViewById(R.id.registerButton);
    MaterialCardView aboutCard = view.findViewById(R.id.card_about);
    MaterialCardView updataCard = view.findViewById(R.id.card_update);
    MaterialCardView helpCard = view.findViewById(R.id.card_help);
    MaterialCardView settingCard = view.findViewById(R.id.card_setting);
    
    
    try {
      // 获取当前应用的包名
      String packageName = getActivity().getPackageName();
      // 获取PackageManager实例
      PackageManager packageManager = getActivity().getPackageManager();
      // 获取应用的PackageInfo对象
      PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
      // 获取版本名称
      currentVersion = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    loginButton.setOnClickListener(
        v -> {
          new MaterialAlertDialogBuilder(getActivity())
              .setTitle("Sorry 抱歉")
              .setMessage("用户功能正在开发中，请持续关注后续版本")
              .setPositiveButton(
                  "确定",
                  (d, w) -> {
                    d.dismiss();
                  })
              .show();
        });
    
    settingCard.setOnClickListener(
        v -> {
          startActivity(new Intent(getActivity(),SettingActivity.class));
        });
    
    helpCard.setOnClickListener(v->{
      startActivity(new Intent(getActivity(),HelpActivity.class));
    });

    registerButton.setOnClickListener(
        v -> {
          new MaterialAlertDialogBuilder(getActivity())
              .setTitle("Sorry 抱歉")
              .setMessage("用户功能正在开发中，请持续关注后续版本")
              .setPositiveButton(
                  "确定",
                  (d, w) -> {
                    d.dismiss();
                  })
              .show();
        });

    aboutCard.setOnClickListener(
        v -> {
          new MaterialAlertDialogBuilder(getActivity())
              .setIcon(R.drawable.ic_about)
              .setTitle("About 关于")
              .setMessage("版本号：" + currentVersion + "\n开发：Fsy\nUI设计：小小工具箱子")
              .setPositiveButton(
                  "确定",
                  (d, w) -> {
                    d.dismiss();
                  })
              .show();
        });

    updataCard.setOnClickListener(
        v -> {
          Toast.makeText(getActivity(), "正在检测更新", Toast.LENGTH_SHORT).show();
          // 初始化更新检查器
          UpdateChecker updateChecker = new UpdateChecker(getActivity(), currentVersion);
          // 检查更新
          updateChecker.checkForUpdate();
        });
    return view;
  }
}
