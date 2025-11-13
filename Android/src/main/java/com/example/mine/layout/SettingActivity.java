package com.example.mine.layout;

import android.content.Intent;
import com.example.mine.*;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mine.tool.Max;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;

public class SettingActivity extends AppCompatActivity {
  private Slider autoclick, autojump, autoplace, entitycrash;
  private MaterialTextView autoclickText, autojumpText, autoplaceText, entitycrashText;
  private MaterialCardView pluginManageCard;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    autoclick = findViewById(R.id.autoclick);
    autojump = findViewById(R.id.autojump);
    autoplace = findViewById(R.id.autoplace);
    entitycrash = findViewById(R.id.entitycrash);

    autoclickText = findViewById(R.id.autoclick_text);
    autojumpText = findViewById(R.id.autojump_text);
    autoplaceText = findViewById(R.id.autoplace_text);
    entitycrashText = findViewById(R.id.entitycrash_text);

    pluginManageCard = findViewById(R.id.plugin_manage_card);

    setSliderTouchListener(autoclick, autoclickText, 0);
    setSliderTouchListener(autoplace, autoplaceText, 1);
    setSliderTouchListener(autojump, autojumpText, 2);
    setSliderTouchListener(entitycrash, entitycrashText, 3);

    // 跳转插件管理页
    pluginManageCard.setOnClickListener(
        v -> {
          Intent pluginManageIntent = new Intent(this, PluginManageIntent.class);
          startActivity(pluginManageIntent);
        });
  }

  private void setSliderTouchListener(Slider view, MaterialTextView text, int id) {
    view.setValue(Max.getInt()[id]);
    text.setText(String.valueOf(Max.getInt()[id]));

    // 添加滑块值变化监听器
    view.addOnChangeListener(
        new Slider.OnChangeListener() {
          @Override
          public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (fromUser) {
              // 用户拖动滑块
              // 在这里可以处理用户拖动滑块时的逻辑
              text.setText(String.valueOf(slider.getValue()));
            }
          }
        });

    // 添加滑块触摸监听器
    view.addOnSliderTouchListener(
        new Slider.OnSliderTouchListener() {
          @Override
          public void onStartTrackingTouch(@NonNull Slider slider) {
            // 当用户开始拖动滑块时调用
          }

          @Override
          public void onStopTrackingTouch(@NonNull Slider slider) {
            // 当用户停止拖动滑块时调用
            int value = (int) slider.getValue();

            // 判断滑块的值是否大于1000
            if (value > 1000) {
              // 弹出MaterialAlertDialogBuilder对话框
              new MaterialAlertDialogBuilder(SettingActivity.this)
                  .setTitle("提示")
                  .setMessage("因为Android机制，当值大于1000时个别设备可能无法适应高频率循环，其循环速度（非CPS）处于900-1200之间波动，用可能导致低端设备崩溃，请酌情考虑是否设置于超高值")
                  .setPositiveButton(
                      "设置",
                      (w, d) -> {
                        Max.setUseintValue(id, value);
                        text.setText(String.valueOf(Max.getInt()[id]));
                      })
                  .setNegativeButton(
                      "回退",
                      (w, d) -> {
                        slider.setValue(Max.getInt()[id]);
                        text.setText(String.valueOf(Max.getInt()[id]));
                      })
                  .show();
            } else {
              Max.setUseintValue(id, value);
              text.setText(String.valueOf(Max.getInt()[id]));
            }
          }
        });
  }
}
