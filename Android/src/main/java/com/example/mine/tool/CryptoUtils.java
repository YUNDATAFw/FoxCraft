package com.example.mine.tool;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {

    /**
     * 支持中文的加解密方法（与PHP encrypt函数完全兼容）
     * @param string 要加密或解密的字符串（支持中文）
     * @param operation 操作类型："E"加密，"D"解密
     * @param key 密钥（凯撒加密的位数）
     * @return 处理后的结果
     */
    public static String encrypt(String string, String operation, String key) {
        int shift = Integer.parseInt(key); // 将密钥转换为整数，作为凯撒加密的位数

        if ("E".equals(operation)) {
            // 加密时：先进行 Base64 编码
            string = Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
            // 再进行凯撒加密
            string = caesarEncryptDecrypt(string, operation, shift);
        } else if ("D".equals(operation)) {
            // 解密时：先进行凯撒解密
            string = caesarEncryptDecrypt(string, operation, shift);
            // 再进行 Base64 解码
            string = new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8);
        }

        return string;
    }

    /**
     * 凯撒加密或解密
     * @param string 要加密或解密的字符串
     * @param operation 操作类型："E"加密，"D"解密
     * @param shift 凯撒加密的位数
     * @return 处理后的结果
     */
    private static String caesarEncryptDecrypt(String string, String operation, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : string.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                int offset = c - base;
                if ("E".equals(operation)) {
                    offset = (offset + shift) % 26;
                } else {
                    offset = (offset - shift + 26) % 26;
                }
                c = (char) (base + offset);
            }
            result.append(c);
        }

        return result.toString();
    }
}
