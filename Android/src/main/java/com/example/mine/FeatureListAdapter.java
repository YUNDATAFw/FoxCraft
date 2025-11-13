package com.example.mine;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import java.util.List;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.RecyclerView;
import com.william.gradient.*;

public class FeatureListAdapter extends RecyclerView.Adapter<FeatureListAdapter.FeatureViewHolder> {
  private List<String> features;

  public FeatureListAdapter(List<String> features) {
    this.features = features;
  }

  @Override
  public FeatureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_item, parent, false);
    return new FeatureViewHolder(view);
  }

  @Override
  public void onBindViewHolder(FeatureViewHolder holder, int position) {
    holder.featureName.setText(features.get(position));

    GradientDrawable gradientDrawable =
        new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, // 渐变方向，从左上到右下
            new int[] {Color.parseColor("#FFACE9FE"), Color.parseColor("#FFCA9EEF")} // 渐变颜色数组
            );
    gradientDrawable.setShape(GradientDrawable.RECTANGLE); // 设置形状为矩形
    holder.Nview.setBackground(gradientDrawable); // 将渐变Drawable设置为View的背景
  }

  @Override
  public int getItemCount() {
    return features.size();
  }

  public void updateFeatures(List<String> newFeatures) {
    this.features = newFeatures;
    notifyDataSetChanged();
  }

  public static class FeatureViewHolder extends RecyclerView.ViewHolder {
    GradientTextView featureName;
    View Nview;
    LinearLayout featureBg;

    public FeatureViewHolder(View itemView) {
      super(itemView);
      featureName = itemView.findViewById(R.id.featureName);
      featureBg = itemView.findViewById(R.id.bg);
      Nview = itemView.findViewById(R.id.view);
    }
  }
}
