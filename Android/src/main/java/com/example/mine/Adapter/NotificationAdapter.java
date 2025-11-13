package com.example.mine.Adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.Item.Notification;
import java.util.List;
import com.example.mine.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.title.setText(notification.getTitle());
        holder.content.setText(notification.getContent());
        holder.content.setVisibility(notification.isExpanded() ? View.VISIBLE : View.GONE);
        holder.icon.setImageResource(notification.isExpanded() ? R.drawable.menu_down : R.drawable.menu_left);

        // 获取颜色值
        int onSurfaceColor = getColorFromAttr(holder.itemView.getContext(), R.attr.onSurfaceColor);
        int onPrimaryContainerColor = getColorFromAttr(holder.itemView.getContext(), R.attr.onPrimaryContainerColor);

        // 设置文本颜色
        holder.title.setTextColor(notification.isExpanded() ? onSurfaceColor : onPrimaryContainerColor);

        // 设置图片的 tint
        ColorFilter colorFilter = new PorterDuffColorFilter(notification.isExpanded() ? onSurfaceColor : onPrimaryContainerColor, PorterDuff.Mode.SRC_IN);
        holder.icon.setColorFilter(colorFilter);

        holder.itemView.setOnClickListener(v -> {
            notification.setExpanded(!notification.isExpanded());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        ImageView icon;

        NotificationViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    private int getColorFromAttr(Context context, int attr) {
        int[] attrs = {attr};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 0); // 默认颜色为 0，表示未设置颜色
        ta.recycle();
        return color;
    }
}
