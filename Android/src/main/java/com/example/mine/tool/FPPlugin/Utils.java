package com.example.mine.tool.FPPlugin;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
  private static final String TAG = "FPPlugin.Utils";
  private static final String PLUGIN_ROOT_DIR = "FPP";
  private static final String PLUGIN_SUB_STATUS_DIR = "sub_plugin_status";
  private static final String PLUGIN_INDEX_FILE = "index";
  private static final String LOG_DIR_NAME = "pluginLog";

  // JSON字段常量
  private static final String KEY_TITLE = "_title";
  private static final String KEY_VERSION = "_version";
  private static final String KEY_DESCRIPTION = "_description";
  private static final String KEY_AUTHOR = "_author";
  private static final String KEY_PLUGINS = "_plugin";

  // plugin数组内对象的字段
  private static final String PLUGIN_FIELD_NAME = "name";
  private static final String PLUGIN_FIELD_ID = "id";
  private static final String PLUGIN_FIELD_VERSION = "version";
  private static final String PLUGIN_FIELD_DESCRIPTION = "description";
  private static final String PLUGIN_FIELD_MODEL = "model";
  private static final String PLUGIN_FIELD_PROGRAM = "program";

  /**
   * 获取所有插件的UUID列表
   *
   * @param context 上下文对象
   * @return UUID列表，若目录不存在或为空则返回null
   */
  public ArrayList<String> getPluginListUUID(Context context) {
    ArrayList<String> returnList = new ArrayList<>();
    File fppDir = new File(context.getFilesDir(), PLUGIN_ROOT_DIR);

    if (!fppDir.exists()) {
      Log.d(TAG, "FPP目录不存在");
      return null;
    }

    File[] folders = fppDir.listFiles(File::isDirectory);
    if (folders != null) {
      for (File folder : folders) {
        returnList.add(folder.getName());
      }
    } else {
      Log.d(TAG, "FPP目录下没有插件文件夹");
      return null;
    }

    return returnList;
  }

  /**
   * 通过UUID解析插件的基本信息
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 包含插件信息的JSONObject，解析失败返回null
   */
  public JSONObject getPluginInfoByUUID(Context context, String uuid) {
    if (uuid == null || uuid.isEmpty()) {
      Log.e(TAG, "UUID为空，无法解析插件信息");
      return null;
    }

    File pluginIndexFile = new File(getPluginDir(context, uuid), PLUGIN_INDEX_FILE);

    if (!pluginIndexFile.exists() || !pluginIndexFile.isFile()) {
      Log.e(TAG, "插件索引文件不存在: " + pluginIndexFile.getAbsolutePath());
      return null;
    }

    StringBuilder contentBuilder = new StringBuilder();
    try (FileReader reader = new FileReader(pluginIndexFile)) {
      char[] buffer = new char[1024];
      int bytesRead;
      while ((bytesRead = reader.read(buffer)) != -1) {
        contentBuilder.append(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      Log.e(TAG, "读取插件文件失败: " + e.getMessage(), e);
      return null;
    }

    try {
      return new JSONObject(contentBuilder.toString().trim());
    } catch (JSONException e) {
      Log.e(TAG, "解析插件JSON失败: " + e.getMessage(), e);
      return null;
    }
  }

  /**
   * 获取子插件开启状态，返回布尔值
   *
   * @param context 上下文
   * @param uuid 插件的UUID
   * @param sub_uuid 子插件的UUID
   * @return 子插件的开启状态
   */
  public boolean getSubPluginStatus(Context context, String uuid, String sub_uuid) {
    if (uuid == null || uuid.isEmpty()) {
      Log.e(TAG, "UUID为空，无法解析插件信息");
      return false;
    }
    File fppSubStatusFile =
        new File(
            context.getFilesDir(),
            String.join("/", PLUGIN_ROOT_DIR, uuid, PLUGIN_SUB_STATUS_DIR, sub_uuid));
    if (fppSubStatusFile == null || !fppSubStatusFile.exists()) {
      return false;
    }
    // 获取文件内容 如果文件内容为 1|true|yes|on 输出true，否则false
    try (BufferedReader reader = new BufferedReader(new FileReader(fppSubStatusFile))) {
      String line = reader.readLine();
      if ("1".equals(line)
          || "true".equalsIgnoreCase(line)
          || "yes".equalsIgnoreCase(line)
          || "on".equalsIgnoreCase(line)) {
        return true;
      }
    } catch (IOException e) {
      Log.e(TAG, "读取文件时发生错误", e);
    }
    return false;
  }

  /**
   * 设置子插件开启状态
   *
   * @param context 上下文
   * @param uuid 插件的UUID
   * @param sub_uuid 子插件的UUID
   * @param status 子插件的状态（true或false）
   * @return 设置是否成功
   */
  public boolean setSubPluginStatus(Context context, String uuid, String sub_uuid, boolean status) {
    if (uuid == null || uuid.isEmpty()) {
      Log.e(TAG, "UUID为空，无法解析插件信息");
      return false;
    }
    File fppSubStatusFile =
        new File(
            context.getFilesDir(),
            String.join("/", PLUGIN_ROOT_DIR, uuid, PLUGIN_SUB_STATUS_DIR, sub_uuid));
    // 如果目录不存在，创建目录
    File parentDir = fppSubStatusFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      Log.e(TAG, "无法创建目录: " + parentDir.getAbsolutePath());
      return false;
    }
    // 写入状态到文件
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fppSubStatusFile))) {
      writer.write(status ? "1" : "0");
      return true;
    } catch (IOException e) {
      Log.e(TAG, "写入文件时发生错误", e);
    }
    return false;
  }

  /**
   * 获取plugin键中的所有插件信息，返回动态数组 每个元素包含name、id、version、description、model和program字段
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 包含插件信息的List<Map>，失败返回null
   */
  public List<Map<String, Object>> getPluginItems(Context context, String uuid) {
    JSONObject pluginInfo = getPluginInfoByUUID(context, uuid);
    if (pluginInfo == null) {
      Log.e(TAG, "无法获取插件基础信息，无法解析plugin字段");
      return null;
    }

    try {
      // 检查是否包含plugin键
      if (!pluginInfo.has(KEY_PLUGINS)) {
        Log.w(TAG, "插件信息中不包含plugin键");
        return new ArrayList<>(); // 返回空列表而非null，表示存在但为空
      }

      JSONArray pluginArray = pluginInfo.getJSONArray(KEY_PLUGINS);
      List<Map<String, Object>> resultList = new ArrayList<>();

      // 遍历plugin数组中的每个对象
      for (int i = 0; i < pluginArray.length(); i++) {
        JSONObject item = pluginArray.getJSONObject(i);
        Map<String, Object> itemMap = new HashMap<>();

        // 提取所需字段，不存在的字段设为null
        itemMap.put(PLUGIN_FIELD_NAME, item.optString(PLUGIN_FIELD_NAME, null));
        itemMap.put(PLUGIN_FIELD_ID, item.optString(PLUGIN_FIELD_ID, null));
        itemMap.put(PLUGIN_FIELD_VERSION, item.optString(PLUGIN_FIELD_VERSION, null));
        itemMap.put(PLUGIN_FIELD_DESCRIPTION, item.optString(PLUGIN_FIELD_DESCRIPTION, null));
        itemMap.put(PLUGIN_FIELD_MODEL, item.optString(PLUGIN_FIELD_MODEL, null));

        // program字段保留原始JSON对象
        if (item.has(PLUGIN_FIELD_PROGRAM)) {
          try {
            itemMap.put(PLUGIN_FIELD_PROGRAM, item.get(PLUGIN_FIELD_PROGRAM));
          } catch (JSONException e) {
            Log.e(TAG, "解析program字段失败，索引: " + i, e);
            itemMap.put(PLUGIN_FIELD_PROGRAM, null);
          }
        } else {
          itemMap.put(PLUGIN_FIELD_PROGRAM, null);
        }

        resultList.add(itemMap);
      }

      Log.d(TAG, "成功解析plugin数组，共 " + resultList.size() + " 项");
      return resultList;

    } catch (JSONException e) {
      Log.e(TAG, "解析plugin数组失败", e);
      return null;
    }
  }

  /**
   * 获取插件的根目录
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 插件目录的File对象
   */
  public File getPluginDir(Context context, String uuid) {
    return new File(new File(context.getFilesDir(), PLUGIN_ROOT_DIR), uuid);
  }

  /**
   * 检查插件是否存在
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 存在返回true，否则返回false
   */
  public boolean isPluginExists(Context context, String uuid) {
    File pluginDir = getPluginDir(context, uuid);
    File indexFile = new File(pluginDir, PLUGIN_INDEX_FILE);
    return pluginDir.exists()
        && pluginDir.isDirectory()
        && indexFile.exists()
        && indexFile.isFile();
  }

  /**
   * 获取插件的名称（从JSON中解析_title字段）
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 插件名称，获取失败返回null
   */
  public String getPluginName(Context context, String uuid) {
    JSONObject pluginInfo = getPluginInfoByUUID(context, uuid);
    if (pluginInfo != null) {
      try {
        return pluginInfo.getString(KEY_TITLE);
      } catch (JSONException e) {
        Log.e(TAG, "获取插件名称失败: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * 获取插件的版本（从JSON中解析_version字段）
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 插件版本，获取失败返回null
   */
  public String getPluginVersion(Context context, String uuid) {
    JSONObject pluginInfo = getPluginInfoByUUID(context, uuid);
    if (pluginInfo != null) {
      try {
        return pluginInfo.getString(KEY_VERSION);
      } catch (JSONException e) {
        Log.e(TAG, "获取插件版本失败: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * 获取插件的介绍（从JSON中解析_description字段）
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 插件介绍，获取失败返回null
   */
  public String getPluginDescription(Context context, String uuid) {
    JSONObject pluginInfo = getPluginInfoByUUID(context, uuid);
    if (pluginInfo != null) {
      try {
        return pluginInfo.getString(KEY_DESCRIPTION);
      } catch (JSONException e) {
        Log.e(TAG, "获取插件介绍失败: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * 获取插件的开发信息（从JSON中解析_author字段）
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @return 开发者信息，获取失败返回null
   */
  public String getPluginAuthor(Context context, String uuid) {
    JSONObject pluginInfo = getPluginInfoByUUID(context, uuid);
    if (pluginInfo != null) {
      try {
        return pluginInfo.getString(KEY_AUTHOR);
      } catch (JSONException e) {
        Log.e(TAG, "获取开发者信息失败: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * 向插件日志文件顶部追加日志，与原有日志保持3行空白距离
   * 日志路径：Android/data/{app包名}/PLUGIN_ROOT_DIR/{uuid}/pluginLog/{time}.log
   *
   * @param context 上下文对象
   * @param uuid 插件的UUID
   * @param location 日志发生位置
   * @param logContent 要写入的日志内容
   * @param formatArgs 日志内容的格式化参数
   * @return 写入是否成功
   */
  public boolean writeLogToPlugin(
      Context context, String uuid, String location, String logContent, Object... formatArgs) {
    if (context == null) {
      Log.e(TAG, "上下文对象为空，无法写入日志");
      return false;
    }
    if (uuid == null || uuid.isEmpty()) {
      Log.e(TAG, "UUID为空，无法写入日志");
      return false;
    }
    if (location == null || location.isEmpty()) {
      location = "Anonymous";
    }

    // 1. 准备时间相关参数
    long timestamp = System.currentTimeMillis();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    dateTimeFormat.setTimeZone(TimeZone.getDefault());
    String dateTimeStr = dateTimeFormat.format(new Date(timestamp));

    // 2. 格式化日志内容
    String formattedLogContent = String.format(logContent, formatArgs);

    // 3. 构建完整日志格式
    String logHeader = String.format("[%s (%d)] %s%n", dateTimeStr, timestamp, location);
    String fullLog = logHeader + formattedLogContent + "\n";

    // 4. 构建日志文件路径（Android/data下的公共目录）
    // 获取Android/data/{app包名}目录
    File externalDataDir = context.getExternalFilesDir(null);
    if (externalDataDir == null) {
      Log.e(TAG, "外部存储不可用，无法写入日志");
      return false;
    }

    // 拼接完整路径：Android/data/{app包名}/PLUGIN_ROOT_DIR/{uuid}/pluginLog
    File pluginLogDir =
        new File(
            externalDataDir,
            PLUGIN_ROOT_DIR + File.separator + uuid + File.separator + LOG_DIR_NAME);
    if (!pluginLogDir.exists() && !pluginLogDir.mkdirs()) {
      Log.e(TAG, "无法创建日志目录: " + pluginLogDir.getAbsolutePath());
      return false;
    }

    // 日志文件名：yyyyMMdd.log（每日一个文件）
    SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
    String fileName = fileDateFormat.format(new Date(timestamp)) + ".log";
    File logFile = new File(pluginLogDir, fileName);

    // 5. 读取现有日志内容
    StringBuilder existingContent = new StringBuilder();
    if (logFile.exists() && logFile.isFile()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
          existingContent.append(line).append("\n");
        }
      } catch (IOException e) {
        Log.e(TAG, "读取现有日志失败: " + e.getMessage(), e);
        return false;
      }
    }

    // 6. 写入新日志（顶部追加 + 3行空白）
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
      writer.write(fullLog);
      writer.write("\n\n\n"); // 与原有日志保持3行空白
      writer.write(existingContent.toString());
      return true;
    } catch (IOException e) {
      Log.e(TAG, "写入日志失败: " + e.getMessage(), e);
      return false;
    }
  }
}
