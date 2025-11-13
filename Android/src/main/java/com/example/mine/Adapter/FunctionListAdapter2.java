package com.example.mine.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.mine.*;
import com.example.mine.Item.*;

public class FunctionListAdapter2 extends RecyclerView.Adapter<FunctionListAdapter2.ViewHolder> {
  private List<FunctionItem2> mData;
  private boolean isAnimating = false; // 动画状态标记，防止重复点击

  public FunctionListAdapter2(List<FunctionItem2> data) {
    this.mData = data;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.service_window_function_list_item2, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FunctionItem2 item = mData.get(position);
    holder.textView.setText(item.getName());
    holder.imageView.setImageResource(item.getIconResId());
    holder.imageView.setImageTintMode(null);

    // 设置点击事件
    holder.itemView.setOnClickListener(
        v -> {
          if (isAnimating) return; // 动画执行中不响应新的点击

          // 执行点击动画序列
          performClickAnimation(
              v,
              () -> {
                // 动画完成后执行实际操作
                item.getAction().run();
                if (item.isEnabled()) {
                  item.setEnabled(false);
                  if (item.getOnDisableAction() != null) {
                    item.getOnDisableAction().run();
                  }
                } else {
                  item.setEnabled(true);
                  if (item.getOnEnableAction() != null) {
                    item.getOnEnableAction().run();
                  }
                }
                notifyItemChanged(position); // 刷新当前项
                isAnimating = false;
              });
        });

    // 动态设置宽高比为 1:1
    holder.itemView.post(
        () -> {
          int width = holder.itemView.getMeasuredWidth();
          ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
          layoutParams.height = width;
          holder.itemView.setLayoutParams(layoutParams);
        });

    // 根据开关状态更新显示
    if (item.getOnEnableAction() == null && item.getOnDisableAction() == null) {
      // 如果没有启用和禁用动作，不修改背景颜色，并将文本内容设置为空白字符
      holder.textView_content.setText("");
      holder.bg.setBackgroundResource(android.R.color.transparent);
    } else {
      // 如果有启用和禁用动作，根据状态更新背景颜色和文本内容
      if (item.isEnabled()) {
        holder.textView_content.setText("开启");
        holder.textView_content.setTextColor(Color.parseColor("#000000"));
        holder.textView.setTextColor(Color.parseColor("#000000"));
        holder.bg.setBackgroundColor(Color.parseColor("#ffffff"));
      } else {
        holder.textView_content.setText("关闭");
        holder.textView_content.setTextColor(Color.parseColor("#ffffff"));
        holder.textView.setTextColor(Color.parseColor("#ffffff"));
        holder.bg.setBackgroundResource(android.R.color.transparent);
      }
    }
  }

  /** 执行点击动画序列：缩小 -> 恢复 -> 快速回弹 */
  private void performClickAnimation(View view, Runnable onComplete) {
    isAnimating = true;

    // 1. 缩小到80%
    ScaleAnimation scaleDown =
        new ScaleAnimation(
            1.0f,
            0.85f,
            1.0f,
            0.85f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
    scaleDown.setDuration(100);
    scaleDown.setFillAfter(true);

    // 2. 恢复到100%
    ScaleAnimation scaleUp =
        new ScaleAnimation(
            0.85f,
            1.0f,
            0.85f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
    scaleUp.setDuration(100);
    scaleUp.setFillAfter(true);
    scaleUp.setStartOffset(100); // 等待缩小动画完成


    // 创建动画集
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.addAnimation(scaleDown);
    animationSet.addAnimation(scaleUp);

    // 设置动画结束监听
    animationSet.setAnimationListener(
        new Animation.AnimationListener() {
          @Override
          public void onAnimationStart(Animation animation) {}

          @Override
          public void onAnimationEnd(Animation animation) {
            if (onComplete != null) {
              onComplete.run();
            }
          }

          @Override
          public void onAnimationRepeat(Animation animation) {}
        });

    // 启动动画
    view.startAnimation(animationSet);
  }

  @Override
  public int getItemCount() {
    return mData.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textView;
    LinearLayout bg;
    TextView textView_content;
    ImageView imageView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.textView);
      bg = itemView.findViewById(R.id.bg);
      textView_content = itemView.findViewById(R.id.textView_content);
      imageView = itemView.findViewById(R.id.imageView);
    }
  }

  // 添加方法设置指定位置的开关状态
  public void setSwitchState(int position, boolean enabled) {
    if (position >= 0 && position < mData.size()) {
      FunctionItem2 item = mData.get(position);
      item.setEnabled(enabled);
      notifyItemChanged(position); // 刷新指定位置的项
    }
  }
}
