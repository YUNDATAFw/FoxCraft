package com.example.mine.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.Adapter.PluginListAdapter;
import com.example.mine.Item.PluginItem;
import com.example.mine.layout.Plugin.IInstallManage;
import com.example.mine.layout.PluginManageIntent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.List;
import com.example.mine.R;

public class PluginListAdapter extends RecyclerView.Adapter<PluginListAdapter.ViewHolder> {
  private List<PluginItem> mData;
  private Context context;

  public PluginListAdapter(List<PluginItem> data, Context context) {
    this.mData = data;
    this.context = context;
  }

  @NonNull
  @Override
  public PluginListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plugin_list, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull PluginListAdapter.ViewHolder holder, int position) {
    PluginItem plugin = mData.get(position);
    holder.pluginName.setText(plugin.getName());
    holder.pluginVersion.setText(plugin.getVersion());
    holder.pluginDescription.setText(plugin.getDescription());
    holder.pluginDelete.setOnClickListener(
        v -> {
          new MaterialAlertDialogBuilder(context)
              .setTitle(context.getString(R.string.plugin_unmount))
              .setMessage(
                  String.format(
                      context.getString(R.string.plugin_sb_unmount),
                      plugin.getName(),
                      plugin.getUUID()))
              .setNegativeButton(
                  context.getString(R.string.plugin_unmount),
                  (w, d) -> {
                    Intent intent = new Intent(context, IInstallManage.class);
                    intent.putExtra(
                        IInstallManage.EXTRA_OPERATION_TYPE, IInstallManage.OPERATION_UNINSTALL);
                    intent.putExtra("uuid", plugin.getUUID());
                    PluginManageIntent.installPluginLauncher.launch(intent);
                  })
              .show();
        });
  }

  @Override
  public int getItemCount() {
    return mData.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView pluginName, pluginVersion, pluginDescription;
    Button pluginDelete, pluginSetting;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      pluginName = itemView.findViewById(R.id.plugin_name);
      pluginVersion = itemView.findViewById(R.id.plugin_version);
      pluginDescription = itemView.findViewById(R.id.plugin_description);
      pluginDelete = itemView.findViewById(R.id.btn_uninstall);
      pluginSetting = itemView.findViewById(R.id.btn_settings);
    }
  }
}
