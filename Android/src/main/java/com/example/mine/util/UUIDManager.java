package com.example.mine.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.util.UUID;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UUIDManager {
    private static final String TAG = "UUIDManager";
    private static final String UUID_FILE_NAME = "uuid._io";
    private final Context context;

    public UUIDManager(Context context) {
        this.context = context;
        initializeUUID();
    }

    /**
     * 初始化UUID文件
     */
    private void initializeUUID() {
        File uuidFile = new File(context.getFilesDir(), UUID_FILE_NAME);
        if (!uuidFile.exists()) {
            // 如果文件不存在，生成一个新的UUID并保存到文件中
            String newUUID = UUID.randomUUID().toString();
            String uuidHash = generateUUIDHash(newUUID);
            saveUUIDToFile(uuidFile, newUUID, uuidHash);
        }
    }

    /**
     * 获取UUID
     *
     * @return UUID字符串
     */
    public String getUUID() {
        File uuidFile = new File(context.getFilesDir(), UUID_FILE_NAME);
        if (!uuidFile.exists()) {
            // 如果文件不存在，重新初始化
            initializeUUID();
        }

        // 从文件中读取UUID和哈希值
        String uuid = readUUIDFromFile(uuidFile);
        String uuidHash = readUUIDHashFromFile(uuidFile);

        // 验证哈希值
        if (uuid != null && uuidHash != null && uuidHash.equals(generateUUIDHash(uuid))) {
            return uuid;
        } else {
            // 如果哈希值不匹配，重置UUID
            Log.e(TAG, "UUID哈希值不匹配，重置UUID");
            resetUUID();
            return getUUID(); // 递归调用，获取新的UUID
        }
    }

    /**
     * 重置UUID
     */
    public void resetUUID() {
        File uuidFile = new File(context.getFilesDir(), UUID_FILE_NAME);
        if (uuidFile.exists()) {
            // 删除旧的UUID文件
            uuidFile.delete();
            Log.d(TAG, "UUID文件已删除，UUID已重置");
        }
        initializeUUID(); // 重新生成UUID
    }

    /**
     * 生成UUID的哈希值
     *
     * @param uuid UUID字符串
     * @return 哈希值
     */
    private String generateUUIDHash(String uuid) {
        try {
            // MD5
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5Digest.digest(uuid.getBytes());
            // SHA256
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] sha256Bytes = sha256Digest.digest(md5Bytes);
            // Base64
            return Base64.encodeToString(sha256Bytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "生成UUID哈希值时出错", e);
            return null;
        }
    }

    /**
     * 将UUID和哈希值保存到文件中
     *
     * @param file     文件对象
     * @param uuid     UUID字符串
     * @param uuidHash 哈希值
     */
    private void saveUUIDToFile(File file, String uuid, String uuidHash) {
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("uuidHash", uuidHash);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.toString().getBytes());
                Log.d(TAG, "UUID和哈希值已保存到文件：" + json.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "保存UUID和哈希值到文件时出错", e);
        }
    }

    /**
     * 从文件中读取UUID
     *
     * @param file 文件对象
     * @return UUID字符串
     */
    private String readUUIDFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            JSONObject json = new JSONObject(new String(bytes));
            return json.getString("uuid");
        } catch (Exception e) {
            Log.e(TAG, "从文件中读取UUID时出错", e);
            return null;
        }
    }

    /**
     * 从文件中读取UUID哈希值
     *
     * @param file 文件对象
     * @return 哈希值
     */
    private String readUUIDHashFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            JSONObject json = new JSONObject(new String(bytes));
            return json.getString("uuidHash");
        } catch (Exception e) {
            Log.e(TAG, "从文件中读取UUID哈希值时出错", e);
            return null;
        }
    }
}
