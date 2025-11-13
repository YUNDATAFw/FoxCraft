package com.example.mine.layout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.Intent;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mine.Adapter.ViewPagerAdapter;
import com.example.mine.Service.ToolService;
import com.example.mine.layout.Fragment.*;
import com.example.mine.util.*;
import com.example.mine.util.opo;
import com.example.mine.widget.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.mine.R;
import com.example.mine.layout.Fragment.*;
import android.Manifest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TogaActivity extends AppCompatActivity {
  private ViewPager2 viewPager;
  private BottomNavigationView bottomNavigation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_toga);
    Intent intent = getIntent();
    if (!intent.getBooleanExtra("assassass", false)) {
      finish();
    }

    viewPager = findViewById(R.id.viewpager);
    bottomNavigation = findViewById(R.id.bottom_navigation);

    // 在Activity或Fragment中调用
    CardKeyManager cardKeyManager = CardKeyManager.getInstance(this);

  setupViewPager();
  setupBottomNavigation();

    // 蜜罐
    // 骗一下逆向小白，特别是找isvip的😁😁😁
    if (intent.getBooleanExtra("isDayVip", false)) {
      pull(intent.getBooleanExtra("isDayVip", false));
    }
    if (intent.getBooleanExtra("isWeekVip", false)) {
      pull(intent.getBooleanExtra("isWeekVip", false));
    }
    if (intent.getBooleanExtra("isMonthVip", false)) {
      pull(intent.getBooleanExtra("isMonthVip", false));
    }
    if (intent.getBooleanExtra("isVip", false)) {
      pull(intent.getBooleanExtra("isVip", false));
    }
    // 蜜罐

    GetString getString = new GetString();
    String[] result =
        getString.getStringsFromKeywordWrapper(TogaActivity.this, getString, "WELCOME_CONTENT");
    if (result != null) {
      StringToast.getInstance(TogaActivity.this)
          .show(
              result[0],
              StringToast.DURATION_LONG,
              StringToast.POSITION_BOTTOM,
              StringToast.TYPE_SUCCESS);
    } else {
      StringToast.getInstance(TogaActivity.this)
          .show(
              "无法连接到Tool库",
              StringToast.DURATION_LONG,
              StringToast.POSITION_TOP,
              StringToast.TYPE_ERROR);
    }

    Intent intent_2 = getIntent();
    if (!intent_2.getBooleanExtra("assassass", false)) {
      finish();
    }
  }

  private void setupViewPager() {
    List<Fragment> fragmentList = new ArrayList<>();
    fragmentList.add(new HomeFragment());
    fragmentList.add(new SettingsFragment());

    ViewPagerAdapter pagerAdapter =
        new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
    viewPager.setAdapter(pagerAdapter);

    viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            bottomNavigation.getMenu().getItem(position).setChecked(true);
          }
        });
  }

  private void setupBottomNavigation() {
    bottomNavigation.setOnNavigationItemSelectedListener(
        item -> {
          int position = 0;
          if (item.getItemId() == R.id.homepage) {
            position = 0;
          } else if (item.getItemId() == R.id.settings) {
            position = 1;
          }
          viewPager.setCurrentItem(position);
          return true;
        });
  }

  // 退出app
  public void pull(boolean is) {
    // 自行实现
  }
}
