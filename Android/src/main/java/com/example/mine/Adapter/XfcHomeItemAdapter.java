package com.example.mine.Adapter;

import androidx.annotation.NonNull;
import com.example.mine.Item.XfcHomeItem;
import java.util.List;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.*;
import android.view.View;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.R;

public class XfcHomeItemAdapter extends RecyclerView.Adapter<XfcHomeItemAdapter.ViewHolder> {
    private List<XfcHomeItem> items;
    private Context context;

    public XfcHomeItemAdapter(Context context, List<XfcHomeItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.xfc_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        XfcHomeItem item = items.get(position);
        
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);

            // 设置点击事件
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // 处理点击事件
                        Toast.makeText(context, "Clicked: " + items.get(position).getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
