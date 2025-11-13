package com.example.mine.layout;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.example.mine.*;
import com.example.mine.util.*;
import com.example.mine.widget.*;

public class SplashActivity extends Activity {
  private TextView countdownTextView;
  private String slh = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppTheme_Red);
    // 创建一个LinearLayout作为根布局
    LinearLayout linearLayout = new LinearLayout(this);
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    linearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.red_background)); // 设置背景颜色
    linearLayout.setGravity(Gravity.CENTER);

    // 设置LinearLayout的布局参数，使其填充整个屏幕
    LayoutParams layoutParams =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    linearLayout.setLayoutParams(layoutParams);

    // 创建一个ImageView来显示Logo
    ImageView logoImageView = new ImageView(this);
    logoImageView.setImageResource(R.mipmap.ic_launcher);
    logoImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // 设置图片缩放类型

    // 设置ImageView的布局参数，使其居中显示
    LayoutParams imageViewParams =
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    imageViewParams.gravity = android.view.Gravity.CENTER;
    logoImageView.setLayoutParams(imageViewParams);

    // 动态设置图片的宽度和高度
    int width = 150; // 宽度为200像素
    int height = 150; // 高度为200像素
    logoImageView.getLayoutParams().width = width;
    logoImageView.getLayoutParams().height = height;

    // 将ImageView添加到LinearLayout中
    linearLayout.addView(logoImageView);

    // 创建一个TextView用于显示倒计时
    countdownTextView = new TextView(this);
    countdownTextView.setText("");
    countdownTextView.setTextSize(18); // 设置文字大小
    countdownTextView.setTextColor(ContextCompat.getColor(this, R.color.red_onSurface)); // 设置文字颜色
    countdownTextView.setGravity(android.view.Gravity.CENTER);

    // 设置TextView的布局参数
    LayoutParams textViewParams =
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    textViewParams.gravity = android.view.Gravity.CENTER;
    textViewParams.setMargins(20, 20, 20, 20); // 设置边距
    countdownTextView.setLayoutParams(textViewParams);

    // 将TextView添加到LinearLayout中
    linearLayout.addView(countdownTextView);

    // 将LinearLayout设置为当前Activity的内容视图
    setContentView(linearLayout);

    // 启动倒计时
    startCountdown();
  }

  private void startCountdown() {
    new CountDownTimer(2000, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        slh = slh + ".";
        // 更新倒计时文本
        countdownTextView.setText("资源配置中" + slh);
      }

      @Override
      public void onFinish() {
        // 倒计时结束后的操作
        UUIDManager uuidManager = new UUIDManager(getApplicationContext());
        String uuid = uuidManager.getUUID();
        countdownTextView.setText("配置完成\nUUID:" + uuid);

        Intent intent = new Intent(SplashActivity.this, TogaActivity.class);
        intent.putExtra("assassass", true);
        intent.putExtra("you", true);
        intent.putExtra("isDayVip", false);
        intent.putExtra("isWeekVip", false);
        intent.putExtra("isMonthVip", false);
        intent.putExtra("isVip", false);
        startActivity(intent);
        finish();
      }
    }.start();
  }
}
