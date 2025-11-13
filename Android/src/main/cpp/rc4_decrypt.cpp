#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <iomanip>

// RC4加密算法的密钥调度算法
void keyScheduling(std::vector<int>& S, const std::string& key) {
    int keyLength = key.size();
    for (int i = 0; i < 256; i++) {
        S[i] = i;  // 初始化S盒
    }

    int j = 0;
    for (int i = 0; i < 256; i++) {
        j = (j + S[i] + key[i % keyLength]) % 256;
        std::swap(S[i], S[j]);
    }
}

// RC4伪随机数生成算法
void pseudoRandomGeneration(std::vector<int>& S, std::vector<int>& keystream, int dataLength) {
    int i = 0, j = 0;
    for (int k = 0; k < dataLength; k++) {
        i = (i + 1) % 256;
        j = (j + S[i]) % 256;
        std::swap(S[i], S[j]);
        keystream.push_back(S[(S[i] + S[j]) % 256]);
    }
}

// 从Base64字符串解码为二进制数据
std::string fromBase64(const std::string& base64) {
    const std::string base64_chars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz"
        "0123456789+/";

    std::string ret;
    int val = 0, valb = -8;
    for (unsigned char c : base64) {
        if (c == '=') break;
        val = (val << 6) + (base64_chars.find(c) & 0x3F);
        valb += 6;
        if (valb >= 0) {
            ret.push_back(char((val >> valb) & 0xFF));
            valb -= 8;
        }
    }
    return ret;
}

// RC4解密函数
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mine_util_m2_m2d(JNIEnv* env, jobject /* this */,jobject context, jbyteArray jKey, jstring jCiphertext) {
    // 从 jbyteArray 提取密钥
    jsize keyLen = env->GetArrayLength(jKey);
    jbyte* keyBytes = env->GetByteArrayElements(jKey, nullptr);
    std::string keyStr(reinterpret_cast<const char*>(keyBytes), keyLen);

    // 从 jstring 提取Base64编码的密文
    const char* base64Ciphertext = env->GetStringUTFChars(jCiphertext, nullptr);
    std::string base64CiphertextStr(base64Ciphertext);

    // 将Base64编码的密文解码为二进制数据
    std::string ciphertext = fromBase64(base64CiphertextStr);

    // 初始化 S 盒
    std::vector<int> S(256);
    keyScheduling(S, keyStr);  // 密钥调度

    // 生成密钥流
    std::vector<int> keystream;
    pseudoRandomGeneration(S, keystream, ciphertext.size());

    // 解密
    std::string decryptedtext;
    for (size_t i = 0; i < ciphertext.size(); i++) {
        decryptedtext += ciphertext[i] ^ keystream[i];  // 按位异或
    }

    // 释放资源
    env->ReleaseByteArrayElements(jKey, keyBytes, 0);
    env->ReleaseStringUTFChars(jCiphertext, base64Ciphertext);
    return env->NewStringUTF(decryptedtext.c_str());
}
