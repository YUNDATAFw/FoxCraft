package com.example.mine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.Item.FunctionItem;
import com.example.mine.*;
import java.util.List;

public class FunctionListAdapter extends RecyclerView.Adapter<FunctionListAdapter.ViewHolder> {
  private List<FunctionItem> mData;
  private Context mContext;
  private int mSelectedPosition = 0; // 默认选中第一个
  private boolean isAnimating = false; // 动画状态标记

  public FunctionListAdapter(Context context, List<FunctionItem> data) {
    this.mContext = context;
    this.mData = data;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.service_window_function_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FunctionItem item = mData.get(position);
    holder.iconImageView.setImageResource(item.getIconResId());
    holder.iconImageView.setImageTintMode(null);
    holder.titleTextView.setText(item.getTitle());

    // 根据当前选中的位置更新图标状态
    if (position == mSelectedPosition) {
      rotateIcon(holder.iconImageView, 45);
      holder.itemLayout.setBackgroundResource(R.color.red_primaryContainer);
    } else {
      rotateIconR(holder.iconImageView, 0);
      holder.itemLayout.setBackgroundResource(android.R.color.transparent);
    }

    // 设置点击事件
    holder.itemView.setOnClickListener(
        v -> {
          if (isAnimating) return; // 防止重复点击

          // 执行点击动画序列
          performClickAnimation(
              v,
              () -> {
                // 动画结束后更新选中状态和执行动作
                mSelectedPosition = position;
                notifyDataSetChanged();

                Runnable action = item.getOnClickAction();
                if (action != null) {
                  action.run();
                }
                isAnimating = false;
              });
        });
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
    ImageView iconImageView;
    TextView titleTextView;
    LinearLayout itemLayout;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      iconImageView = itemView.findViewById(R.id.imageView);
      titleTextView = itemView.findViewById(R.id.textView);
      itemLayout = itemView.findViewById(R.id.bg);
    }
  }

  // 旋转图标的方法
  private void rotateIcon(ImageView imageView, float toDegree) {
    Animation rotation =
        new RotateAnimation(
            0, toDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    rotation.setDuration(300);
    rotation.setFillAfter(true);
    imageView.startAnimation(rotation);
  }

  // 恢复旋转图标的方法
  private void rotateIconR(ImageView imageView, float toDegree) {
    Animation rotation =
        new RotateAnimation(
            toDegree, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    rotation.setDuration(300);
    rotation.setFillAfter(true);
    imageView.startAnimation(rotation);
  }
}
