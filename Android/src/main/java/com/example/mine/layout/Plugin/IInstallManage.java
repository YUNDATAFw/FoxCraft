package com.example.mine.layout.Plugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import com.example.mine.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.io.File;

public class IInstallManage extends AppCompatActivity {
    private static final int READ_JSON_FILE_REQUEST = 1001;
    private static final String TAG = "IInstallManage";
    // 新增操作类型常量
    public static final String EXTRA_OPERATION_TYPE = "operation_type";
    public static final int OPERATION_INSTALL = 1;
    public static final int OPERATION_UNINSTALL = 2;
    
    private DocumentFile pluginFile;
    private TextView titleView,tipView,logText;
    private String uuid;
    private int operationType; // 当前操作类型

    // 解析回调接口
    public interface ParseCallback {
        void onSuccess(String Message);
        void onFailure(String errorMessage);
    }

    // 安装回调接口
    public interface InstallCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // 新增卸载回调接口
    public interface UninstallCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_i_install_manage);
        logText = findViewById(R.id.logMessage);
        titleView = findViewById(R.id.title);
        tipView = findViewById(R.id.tip);
        
        // 获取传入的操作类型，默认是安装
        operationType = getIntent().getIntExtra(EXTRA_OPERATION_TYPE, OPERATION_INSTALL);
        
        titleView.setText("安装管理器 ("+(operationType == OPERATION_INSTALL ? "安装" : "卸载")+")");
        tipView.setText((operationType == OPERATION_INSTALL ? "您正在安装/更新插件，请注意您的设备安全" : "您即将卸载插件，我们并不会保留副本，他会消失，永远永远"));
        logMsg(TAG, "INFO", "启动插件" + (operationType == OPERATION_INSTALL ? "安装" : "卸载") + "管理器");
        
        // 根据操作类型执行不同初始化逻辑
        if (operationType == OPERATION_INSTALL) {
            // 安装操作：打开文件选择器
            openFilePicker();
        } else {
            // 卸载操作：获取传入的UUID
            uuid = getIntent().getStringExtra("uuid");
            if (uuid == null || uuid.isEmpty()) {
                logMsg(TAG, "ERROR", "卸载失败：UUID为空");
                logMsg("未指定要卸载的插件UUID");
                setupCloseButton();
            } else {
                logMsg(TAG, "INFO", "准备卸载插件，UUID: " + uuid);
                setupUninstallUI();
            }
        }
    }

    /** 打开文件选择器，限定选择JSON文件 */
    private void openFilePicker() {
        logMsg(TAG, "INFO", "打开文件选择器，选择JSON格式插件文件");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, READ_JSON_FILE_REQUEST);
    }

    /** 设置卸载操作的UI */
    private void setupUninstallUI() {
        Button closeButton = findViewById(R.id.close);
        Button uninstallButton = findViewById(R.id.install);
        uninstallButton.setText("卸载");
        uninstallButton.setVisibility(View.VISIBLE);
        
        closeButton.setOnClickListener(v -> finish());
        uninstallButton.setOnClickListener(v -> {
            uninstallButton.setVisibility(View.GONE);
            closeButton.setText("返回");
            uninstallPlugin(new UninstallCallback() {
                @Override
                public void onSuccess(String message) {
                    logMsg(message);
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    logMsg("卸载失败: " + errorMessage);
                }
            });
        });
    }

    /** 设置仅显示关闭按钮 */
    private void setupCloseButton() {
        Button closeButton = findViewById(R.id.close);
        Button installButton = findViewById(R.id.install);
        installButton.setVisibility(View.GONE);
        closeButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 只有安装操作需要处理文件选择结果
        if (operationType != OPERATION_INSTALL) {
            return;
        }

        logMsg(TAG, "INFO", "收到文件选择结果，处理中...");

        if (requestCode == READ_JSON_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                logMsg(TAG, "INFO", "用户成功选择文件");
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        logMsg(TAG, "INFO", "获取到文件URI: " + uri.toString());
                        pluginFile = DocumentFile.fromSingleUri(this, uri);

                        if (pluginFile != null) {
                            logMsg(TAG, "INFO", "文件对象创建成功，检查文件是否存在");
                            if (pluginFile.exists()) {
                                logMsg(TAG, "INFO", "文件存在，开始解析插件信息");
                                parse(new ParseCallback() {
                                    @Override
                                    public void onSuccess(String Message) {
                                        logMsg("解析成功:\n" + Message);
                                        logMsg(TAG, "INFO", "解析完成，准备进行安装操作");
                                        Button closeButton = findViewById(R.id.close);
                                        closeButton.setOnClickListener(v -> finish());
                                        Button installButton = findViewById(R.id.install);
                                        installButton.setOnClickListener(v -> {
                                            installButton.setVisibility(View.GONE);
                                            closeButton.setText("返回");
                                            installPlugin(new InstallCallback() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    logMsg(message);
                                                }

                                                @Override
                                                public void onFailure(String errorMessage) {
                                                    logMsg("操作失败: " + errorMessage);
                                                }
                                            });
                                        });
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        logMsg("插件解析失败: " + errorMessage);
                                        logMsg(TAG, "ERROR", "解析失败详情: " + errorMessage);
                                        setupCloseButton();
                                    }
                                });
                            } else {
                                logMsg(TAG, "ERROR", "文件不存在");
                                logMsg("文件不存在或无法访问");
                                setupCloseButton();
                            }
                        } else {
                            logMsg(TAG, "ERROR", "无法创建DocumentFile对象");
                            logMsg("无法处理选择的文件");
                            setupCloseButton();
                        }
                    } else {
                        logMsg(TAG, "ERROR", "获取到的文件URI为空");
                        logMsg("未选择有效的文件");
                        setupCloseButton();
                    }
                } else {
                    logMsg(TAG, "ERROR", "返回的Intent数据为空");
                    logMsg("未获取到文件信息");
                    setupCloseButton();
                }
            } else if (resultCode == RESULT_CANCELED) {
                logMsg(TAG, "INFO", "用户取消了文件选择");
                logMsg("已取消文件选择");
                setupCloseButton();
            } else {
                logMsg(TAG, "ERROR", "文件选择返回未知结果码: " + resultCode);
                logMsg("文件选择失败，错误代码: " + resultCode);
                setupCloseButton();
            }
        }
    }

    /*
     * log输出
     */
    private void logMsg(String error) {
        logText.setText(logText.getText() + "\n" + String.format("%s", error));
    }

    private void logMsg(String Tag, String error) {
        logText.setText(logText.getText() + "\n" + String.format("<%s> %s", Tag, error));
    }

    private void logMsg(String Tag, String Level, String error) {
        logText.setText(logText.getText() + "\n" + String.format("<%s> %s %s", Tag, Level, error));
    }

    /**
     * 解析插件
     *
     * @param callback 解析结果回调
     */
    private void parse(ParseCallback callback) {
        logMsg(TAG, "INFO", "开始解析插件文件");

        if (pluginFile == null) {
            logMsg(TAG, "ERROR", "解析失败：pluginFile为空");
            callback.onFailure("文件为空，无法解析");
            return;
        }

        String jsonContent = readFileContent();
        if (jsonContent == null) {
            logMsg(TAG, "ERROR", "读取文件内容返回null");
            callback.onFailure("无法读取插件文件");
            return;
        }
        logMsg(TAG, "INFO", "文件内容读取成功，长度: " + jsonContent.length() + " 字符");

        try {
            logMsg(TAG, "INFO", "开始解析JSON内容");
            JSONObject pluginJson = new JSONObject(jsonContent.trim());
            String pluginName = pluginJson.getString("_title");
            String fileName = pluginFile.getName();
            logMsg(TAG, "INFO", "获取到文件名: " + fileName);

            uuid = removeSuffix(fileName, ".plugin.json");
            logMsg(TAG, "INFO", "从文件名提取UUID: " + uuid);

            if (fileName.equals(uuid)) {
                String errorMsg = String.format("文件命名错误，无法从 '%s' 中获取UUID", fileName);
                logMsg(TAG, "ERROR", errorMsg);
                callback.onFailure(errorMsg);
                return;
            }

            StringBuilder pluginInfo = new StringBuilder();
            pluginInfo.append("Plugin Name: ").append(pluginName).append("\n");
            pluginInfo.append("File Name: ").append(fileName).append("\n");
            pluginInfo.append("UUID: ").append(uuid).append("\n");

            logMsg(TAG, "INFO", "插件解析成功");
            callback.onSuccess(pluginInfo.toString());
        } catch (JSONException e) {
            String errorMsg = "JSON解析错误: " + e.getMessage();
            logMsg(TAG, "ERROR", errorMsg + "，异常详情: " + Log.getStackTraceString(e));
            callback.onFailure(errorMsg);
        }
    }

    /** 安装或更新插件 */
    private void installPlugin(InstallCallback callback) {
        logMsg(TAG, "INFO", "开始执行插件安装/更新操作");

        if (uuid == null || uuid.isEmpty()) {
            String errorMsg = "UUID为空，无法执行操作";
            logMsg(TAG, "ERROR", errorMsg);
            callback.onFailure(errorMsg);
            return;
        }
        logMsg(TAG, "INFO", "操作目标UUID: " + uuid);

        if (pluginFile == null) {
            String errorMsg = "插件文件对象为空";
            logMsg(TAG, "ERROR", errorMsg);
            callback.onFailure(errorMsg);
            return;
        }

        if (!pluginFile.exists()) {
            String errorMsg = "插件文件不存在";
            logMsg(TAG, "ERROR", errorMsg);
            callback.onFailure(errorMsg);
            return;
        }

        try {
            File fppDir = new File(getFilesDir(), "FPP");
            logMsg(TAG, "INFO", "目标根目录: " + fppDir.getAbsolutePath());

            if (!fppDir.exists() && !fppDir.mkdirs()) {
                String errorMsg = "无法创建FPP目录";
                logMsg(TAG, "ERROR", errorMsg);
                callback.onFailure(errorMsg);
                return;
            }

            File pluginDir = new File(fppDir, uuid);
            boolean isUpdate = pluginDir.exists();
            String operationTypeStr = isUpdate ? "更新" : "安装";
            logMsg(TAG, "INFO", "检测到" + (isUpdate ? "已存在" : "不存在") + "UUID目录，执行" + operationTypeStr + "操作");

            if (!pluginDir.exists() && !pluginDir.mkdirs()) {
                String errorMsg = "无法创建UUID目录: " + uuid;
                logMsg(TAG, "ERROR", errorMsg);
                callback.onFailure(errorMsg);
                return;
            }

            File targetFile = new File(pluginDir, "index");
            logMsg(TAG, "INFO", "目标文件路径: " + targetFile.getAbsolutePath());

            if (isUpdate && targetFile.exists() && !targetFile.delete()) {
                logMsg(TAG, "WARNING", "无法删除原有文件，可能导致更新失败");
            }

            try (InputStream inputStream = getContentResolver().openInputStream(pluginFile.getUri());
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytes = 0;

                logMsg(TAG, "INFO", "开始复制文件内容");
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                logMsg(TAG, "INFO", "文件复制完成，总大小: " + totalBytes + " 字节");
                callback.onSuccess(operationTypeStr + "成功: 插件已" + operationTypeStr + "至 " + targetFile.getAbsolutePath());

            } catch (IOException e) {
                String errorMsg = "文件复制失败: " + e.getMessage();
                logMsg(TAG, "ERROR", errorMsg + "，异常详情: " + Log.getStackTraceString(e));
                callback.onFailure(errorMsg);
            }

        } catch (Exception e) {
            String errorMsg = "操作过程出错: " + e.getMessage();
            logMsg(TAG, "ERROR", errorMsg + "，异常详情: " + Log.getStackTraceString(e));
            callback.onFailure(errorMsg);
        }
    }

    /** 新增：卸载插件逻辑 */
    private void uninstallPlugin(UninstallCallback callback) {
        logMsg(TAG, "INFO", "开始执行插件卸载操作，UUID: " + uuid);

        if (uuid == null || uuid.isEmpty()) {
            String errorMsg = "UUID为空，无法执行卸载操作";
            logMsg(TAG, "ERROR", errorMsg);
            callback.onFailure(errorMsg);
            return;
        }

        try {
            // 获取插件目录
            File fppDir = new File(getFilesDir(), "FPP");
            File pluginDir = new File(fppDir, uuid);
            
            if (!pluginDir.exists()) {
                String errorMsg = "插件目录不存在，无需卸载: " + pluginDir.getAbsolutePath();
                logMsg(TAG, "INFO", errorMsg);
                callback.onFailure(errorMsg);
                return;
            }

            // 删除目录下所有文件
            deleteDir(pluginDir);
            
            if (!pluginDir.exists()) {
                logMsg(TAG, "INFO", "插件目录已成功删除");
                callback.onSuccess("卸载成功: 插件 " + uuid + " 已完全移除");
            } else {
                String errorMsg = "卸载不完全，部分文件可能未被删除";
                logMsg(TAG, "WARNING", errorMsg);
                callback.onFailure(errorMsg);
            }

        } catch (Exception e) {
            String errorMsg = "卸载过程出错: " + e.getMessage();
            logMsg(TAG, "ERROR", errorMsg + "，异常详情: " + Log.getStackTraceString(e));
            callback.onFailure(errorMsg);
        }
    }

    /** 递归删除目录及其中的所有文件 */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    boolean success = deleteDir(child);
                    if (!success) {
                        logMsg(TAG, "WARNING", "无法删除文件: " + child.getAbsolutePath());
                        return false;
                    }
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 读取文件内容
     *
     * @return 文件内容字符串，失败返回null
     */
    private String readFileContent() {
        try (InputStream inputStream = getContentResolver().openInputStream(pluginFile.getUri());
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            logMsg(TAG, "INFO", "文件内容读取完成");
            return stringBuilder.toString();

        } catch (IOException e) {
            String errorMsg = "读取文件失败: " + e.getMessage();
            logMsg(TAG, "ERROR", errorMsg + "，异常详情: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * 去除字符串末尾的指定后缀
     */
    public static String removeSuffix(String originalString, String suffix) {
        if (originalString == null || suffix == null) {
            return originalString;
        }

        if (originalString.endsWith(suffix)) {
            return originalString.substring(0, originalString.length() - suffix.length());
        }

        return originalString;
    }
}