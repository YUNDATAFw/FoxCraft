package com.example.mine.tool;

import android.content.Context;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Function {
    private static final String ASSETS_FILE_NAME = "Function.json";
    private static final String DATA_FILE_NAME = "Function.json";

    private Map<String, JSONObject> functionMap;
    private String dataFilePath;

    public Function(Context context) {
        functionMap = new HashMap<>();
        // 获取应用的私有目录路径
        dataFilePath = context.getFilesDir().getAbsolutePath() + "/" + DATA_FILE_NAME;
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists()) {
            // 如果私有目录中的json配置不存在，从assets复制过去
            copyFileFromAssetsToData(context, ASSETS_FILE_NAME, dataFilePath);
        }
        loadFunctions(dataFilePath);
    }

    private void copyFileFromAssetsToData(Context context, String assetsFileName, String dataFilePath) {
        try {
            File dataDir = new File(dataFilePath).getParentFile();
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            InputStream in = context.getAssets().open(assetsFileName);
            FileOutputStream out = new FileOutputStream(dataFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFunctions(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            fis.close();
            JSONObject jsonObject = new JSONObject(sb.toString());
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                functionMap.put(key, jsonObject.getJSONObject(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getStatus(String functionName) {
        JSONObject jsonObject = functionMap.get(functionName);
        if (jsonObject == null) {
            // 如果没有这个键，创建一个默认的JSON对象并保存
            jsonObject = new JSONObject();
            try {
                jsonObject.put("status", false);
                jsonObject.put("frequency", 0);
                functionMap.put(functionName, jsonObject);
                saveFunctions(dataFilePath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        return jsonObject.optBoolean("status", false);
    }

    public int getFrequency(String functionName) {
        JSONObject jsonObject = functionMap.get(functionName);
        if (jsonObject == null) {
            // 如果没有这个键，创建一个默认的JSON对象并保存
            jsonObject = new JSONObject();
            try {
                jsonObject.put("status", false);
                jsonObject.put("frequency", 0);
                functionMap.put(functionName, jsonObject);
                saveFunctions(dataFilePath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return jsonObject.optInt("frequency", -1);
    }

    public void setStatus(String functionName, boolean status) {
        JSONObject jsonObject = functionMap.get(functionName);
        if (jsonObject != null) {
            try {
                jsonObject.put("status", status);
                saveFunctions(dataFilePath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFrequency(String functionName, int frequency) {
        JSONObject jsonObject = functionMap.get(functionName);
        if (jsonObject != null) {
            try {
                jsonObject.put("frequency", frequency);
                saveFunctions(dataFilePath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFunctions(String filePath) {
        try {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, JSONObject> entry : functionMap.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(jsonObject.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
