package com.example.mine.layout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mine.Adapter.PluginListAdapter;
import com.example.mine.Item.PluginItem;
import com.example.mine.R;
import com.example.mine.layout.Plugin.IInstallManage;
import com.example.mine.tool.FPPlugin.Utils;
import java.util.ArrayList;
import java.util.List;

public class PluginManageIntent extends AppCompatActivity {

  public static ActivityResultLauncher<Intent> installPluginLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_plugin_manage_intent);

    // 初始化 launcher
    installPluginLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              // 当从 IInstallManage 返回时，会触发此回调
              // 刷新插件列表
              loadPluginList();
            });

    Button addPlugin = findViewById(R.id.add);
    addPlugin.setOnClickListener(
        v -> {
          // 启动插件管理器 安装模式
          Intent intent = new Intent(PluginManageIntent.this, IInstallManage.class);
          intent.putExtra(IInstallManage.EXTRA_OPERATION_TYPE, IInstallManage.OPERATION_INSTALL);
          // 使用 launcher 启动活动
          installPluginLauncher.launch(intent);
        });

    loadPluginList();
  }

  private void loadPluginList() {
    RecyclerView pluginList = findViewById(R.id.pluginList);
    pluginList.setLayoutManager(new LinearLayoutManager(this));
    // 初始化数据
    List<PluginItem> pluginListData = new ArrayList<>();

    Utils pluginUtils = new Utils();
    ArrayList<String> pluginUUIDs = pluginUtils.getPluginListUUID(this); // 传入上下文

    // 处理UUID列表
    if (pluginUUIDs != null && !pluginUUIDs.isEmpty()) {
      // 列表不为空，遍历所有UUID
      for (String uuid : pluginUUIDs) {
        // 打印UUID

        // 可以结合其他方法获取插件详细信息
        String pluginName = pluginUtils.getPluginName(this, uuid);
        String pluginVersion = pluginUtils.getPluginVersion(this, uuid);
        String pluginDescription = pluginUtils.getPluginDescription(this, uuid);
        String pluginUUID = uuid;
        pluginListData.add(
            new PluginItem(pluginUUID, pluginName, pluginVersion, pluginDescription));
      }
    } else {
      // 列表为空或不存在插件

    }
    PluginListAdapter adapter = new PluginListAdapter(pluginListData, this);
    pluginList.setAdapter(adapter);
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // 可以根据 requestCode 和 resultCode 过滤特定场景的刷新
    if (requestCode == 100 && resultCode == RESULT_OK) {
      loadPluginList(); // 刷新列表
    }
  }
}
