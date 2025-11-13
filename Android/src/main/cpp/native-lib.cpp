#include <jni.h>
#include <string>
#include <unordered_map>
#include <vector>
#include <sstream>
#include <cstdint>
#include <android/log.h>
#include <stdexcept>

static int callCount = 0;

// -------------------------- 新增：凯撒解密（偏移量与ascll.cpp的加密一致，key=3） --------------------------
std::string caesarDecrypt(const std::string& input, int key = 3) {
    std::string result;
    for (char c : input) {
        if (c >= 'A' && c <= 'Z') {
            // 大写字母解密：处理负数（如A-3→X，而非负数）
            result += (c - 'A' - key + 26) % 26 + 'A';
        } else if (c >= 'a' && c <= 'z') {
            // 小写字母解密
            result += (c - 'a' - key + 26) % 26 + 'a';
        } else if (c >= '0' && c <= '9') {
            // 数字解密（与加密对应）
            result += (c - '0' - key + 10) % 10 + '0';
        } else if (c == '+' || c == '/' || c == '=') {
            // Base64特殊字符：保留（与加密对应）
            result += c;
        } else {
            // 其他字符：直接保留
            result += c;
        }
    }
    return result;
}

// -------------------------- 原有：Base64解码表（通过函数初始化，避免NDK编译错误） --------------------------
static std::unordered_map<char, uint8_t> createBase64DecodeMap() {
    std::unordered_map<char, uint8_t> map;
    map.insert({'A', 0}); map.insert({'B', 1}); map.insert({'C', 2}); map.insert({'D', 3});
    map.insert({'E', 4}); map.insert({'F', 5}); map.insert({'G', 6}); map.insert({'H', 7});
    map.insert({'I', 8}); map.insert({'J', 9}); map.insert({'K', 10}); map.insert({'L', 11});
    map.insert({'M', 12}); map.insert({'N', 13}); map.insert({'O', 14}); map.insert({'P', 15});
    map.insert({'Q', 16}); map.insert({'R', 17}); map.insert({'S', 18}); map.insert({'T', 19});
    map.insert({'U', 20}); map.insert({'V', 21}); map.insert({'W', 22}); map.insert({'X', 23});
    map.insert({'Y', 24}); map.insert({'Z', 25}); map.insert({'a', 26}); map.insert({'b', 27});
    map.insert({'c', 28}); map.insert({'d', 29}); map.insert({'e', 30}); map.insert({'f', 31});
    map.insert({'g', 32}); map.insert({'h', 33}); map.insert({'i', 34}); map.insert({'j', 35});
    map.insert({'k', 36}); map.insert({'l', 37}); map.insert({'m', 38}); map.insert({'n', 39});
    map.insert({'o', 40}); map.insert({'p', 41}); map.insert({'q', 42}); map.insert({'r', 43});
    map.insert({'s', 44}); map.insert({'t', 45}); map.insert({'u', 46}); map.insert({'v', 47});
    map.insert({'w', 48}); map.insert({'x', 49}); map.insert({'y', 50}); map.insert({'z', 51});
    map.insert({'0', 52}); map.insert({'1', 53}); map.insert({'2', 54}); map.insert({'3', 55});
    map.insert({'4', 56}); map.insert({'5', 57}); map.insert({'6', 58}); map.insert({'7', 59});
    map.insert({'8', 60}); map.insert({'9', 61}); map.insert({'+', 62}); map.insert({'/', 63});
    map.insert({'=', 64}); // 填充字符标记
    return map;
}
static const std::unordered_map<char, uint8_t> base64DecodeMap = createBase64DecodeMap();

// -------------------------- 原有：Base64解码函数 --------------------------
std::vector<uint8_t> base64Decode(const std::string& input) {
    std::vector<uint8_t> output;
    size_t inputSize = input.size();
    size_t i = 0;
    while (i < inputSize) {
        // 读取4个Base64字符（不足补填充标记64）
        uint8_t c1 = 64, c2 = 64, c3 = 64, c4 = 64;
        if (i < inputSize) {
            auto it = base64DecodeMap.find(input[i++]);
            if (it != base64DecodeMap.end()) c1 = it->second;
        }
        if (i < inputSize) {
            auto it = base64DecodeMap.find(input[i++]);
            if (it != base64DecodeMap.end()) c2 = it->second;
        }
        if (i < inputSize) {
            auto it = base64DecodeMap.find(input[i++]);
            if (it != base64DecodeMap.end()) c3 = it->second;
        }
        if (i < inputSize) {
            auto it = base64DecodeMap.find(input[i++]);
            if (it != base64DecodeMap.end()) c4 = it->second;
        }

        // -------------------------- 修正后的核心逻辑 --------------------------
        uint32_t triple = (c1 << 18) | (c2 << 12) | (c3 << 6) | c4;
        // 2个填充符（c3=64且c4=64）：输出1字节
        if (c3 == 64 && c4 == 64) {
            output.push_back((triple >> 16) & 0xFF);
        }
        // 1个填充符（c4=64但c3≠64）：输出2字节
        else if (c4 == 64) {
            output.push_back((triple >> 16) & 0xFF);
            output.push_back((triple >> 8) & 0xFF);
        }
        // 无填充符：输出3字节
        else {
            output.push_back((triple >> 16) & 0xFF);
            output.push_back((triple >> 8) & 0xFF);
            output.push_back(triple & 0xFF);
        }
        // ----------------------------------------------------------------------
    }
    return output;
}

// -------------------------- 修改：字节数组→普通字符串（新增凯撒解密步骤） --------------------------
std::string bytesToString(const std::vector<uint8_t>& bytes) {
    // 步骤1：字节数组→字符串（即ascll.cpp输出的“凯撒加密后的Base64字符串”）
    std::string encryptedBase64(bytes.begin(), bytes.end());
    // 步骤2：凯撒解密（还原为原始Base64字符串）
    std::string rawBase64 = caesarDecrypt(encryptedBase64, 3); // 偏移量3，与加密一致
    // 步骤3：Base64解码（还原为原始普通字符串）
    std::vector<uint8_t> rawBytes = base64Decode(rawBase64);
    // 步骤4：字节→字符串（最终原始结果）
    return std::string(rawBytes.begin(), rawBytes.end());
}

// -------------------------- 原有：全局键值对（需替换为ascll.cpp输出的加密后十六进制数组） --------------------------
// ========================= 加密配置说明 =========================
// 1. 加密流程：普通UTF-8字符串 → Base64编码 → 凯撒加密（偏移量key=3）                                      
// 2. 解密流程（JNI端）：十六进制数组 → 字符串 → 凯撒解密 → Base64解码 → 原始字符串
// 3. 直接复制下方代码到 native-lib.cpp 中替换原有 keywordMap 即可
// ================================================================

// 需自行生成键值对，生成方法看readme说明

std::unordered_map<std::string, std::vector<std::vector<uint8_t>>> keywordMap = {
    {"INFO", {{0x43, 0x41, 0x6B, 0x73, 0x67, 0x44, 0x3D, 0x3D}, {0x42, 0x35, 0x32, 0x7A}, {0x58, 0x34, 0x55, 0x47, 0x59, 0x33, 0x59, 0x57, 0x56, 0x58, 0x55, 0x4C, 0x58, 0x6A, 0x3D, 0x3D}, {0x43, 0x36, 0x45, 0x70, 0x65, 0x36, 0x59, 0x7A, 0x67, 0x35, 0x32, 0x33, 0x67, 0x44, 0x3D, 0x3D}, {0x64, 0x70, 0x49, 0x35, 0x42, 0x56, 0x32, 0x76, 0x42, 0x5A, 0x38, 0x71, 0x4F, 0x34, 0x51, 0x38, 0x66, 0x36, 0x55, 0x6F, 0x65, 0x54, 0x3D, 0x3D}, {0x64, 0x70, 0x49, 0x35, 0x42, 0x56, 0x32, 0x76, 0x42, 0x5A, 0x38, 0x71, 0x4F, 0x34, 0x51, 0x33, 0x66, 0x70, 0x6F, 0x78, 0x43, 0x7A, 0x3D, 0x3D}, {0x42, 0x5A, 0x38, 0x6E, 0x66, 0x70, 0x32, 0x73, 0x43, 0x46, 0x38, 0x79, 0x66, 0x62, 0x38, 0x54, 0x66, 0x70, 0x32, 0x6D, 0x43, 0x41, 0x51, 0x63}, {0x65, 0x41, 0x6F, 0x54, 0x64, 0x5A, 0x54, 0x3D}, {0x64, 0x35, 0x6F, 0x76, 0x65, 0x49, 0x45, 0x62, 0x65, 0x35, 0x51, 0x6F, 0x66, 0x36, 0x50, 0x3D}}},
    {"MIKU", {{0x39, 0x43, 0x62, 0x51, 0x42, 0x35, 0x32, 0x77, 0x4F, 0x78, 0x70, 0x66, 0x6D, 0x5A, 0x59, 0x37, 0x42, 0x5A, 0x36, 0x73, 0x71, 0x4C, 0x34, 0x7A, 0x65, 0x4A, 0x41, 0x73, 0x71, 0x4C, 0x33, 0x78, 0x65, 0x5A, 0x6F, 0x78, 0x43, 0x56, 0x30, 0x73, 0x71, 0x4C, 0x34, 0x57, 0x43, 0x41, 0x4D, 0x35, 0x64, 0x5A, 0x51, 0x6F, 0x39, 0x43, 0x62, 0x51, 0x4F, 0x6F, 0x55, 0x79, 0x65, 0x2B, 0x70, 0x66, 0x6D, 0x5A, 0x61, 0x57, 0x43, 0x41, 0x4F, 0x73, 0x71, 0x4C, 0x34, 0x35, 0x64, 0x5A, 0x51, 0x6F}, {0x4E, 0x48, 0x6E, 0x73, 0x59, 0x6A, 0x3D, 0x3D}, {0x4E, 0x46, 0x6F, 0x5A}}},
    {"FOG_COLOR", {{0x4C, 0x63, 0x44, 0x7A, 0x43, 0x70, 0x43, 0x70, 0x43, 0x70, 0x43, 0x70}, {0x4C, 0x63, 0x4C, 0x62, 0x43, 0x6D, 0x44, 0x7A, 0x50, 0x47, 0x58, 0x35}, {0x4C, 0x63, 0x4C, 0x62, 0x43, 0x70, 0x43, 0x6C, 0x51, 0x6D, 0x49, 0x6F}, {0x4C, 0x63, 0x4C, 0x62, 0x51, 0x4A, 0x4C, 0x34, 0x42, 0x35, 0x50, 0x33}, {0x4C, 0x63, 0x4C, 0x62, 0x50, 0x57, 0x43, 0x6B, 0x52, 0x57, 0x58, 0x61}, {0x4C, 0x63, 0x4C, 0x62, 0x51, 0x47, 0x6B, 0x6D, 0x50, 0x4A, 0x48, 0x63}, {0x4C, 0x63, 0x4C, 0x62, 0x52, 0x4A, 0x54, 0x33, 0x42, 0x70, 0x4D, 0x6C}}},
    {"WELCOME_CONTENT", {{0x38, 0x74, 0x62, 0x6C, 0x39, 0x4F, 0x2B, 0x52, 0x38, 0x4F, 0x35, 0x2F, 0x38, 0x38, 0x56, 0x72, 0x55, 0x70, 0x32, 0x37, 0x4C, 0x54, 0x3D, 0x3D}, {0x67, 0x4B, 0x4D, 0x34, 0x43, 0x54, 0x3D, 0x3D}}}
};

// -------------------------- 新增：抽取公共字符串获取逻辑 --------------------------
std::vector<std::string> getDecryptedStrings(const std::string& keyword) {
    std::vector<std::string> result;
    auto it = keywordMap.find(keyword);
    if (it != keywordMap.end()) {
        for (const auto& bytes : it->second) {
            result.push_back(bytesToString(bytes)); // 复用现有解密逻辑
        }
    }
    return result;
}

// -------------------------- 原有：调用Java层cop方法 --------------------------
void callJavaCopMethod(JNIEnv* env, jobject getString) {
    if (getString == nullptr) return;
    jclass cls = env->GetObjectClass(getString);
    if (cls == nullptr) return;
    jmethodID mid = env->GetMethodID(cls, getDecryptedStrings("INFO")[1].c_str(), getDecryptedStrings("MIKU")[2].c_str());
    if (mid != nullptr) env->CallVoidMethod(getString, mid);
    env->DeleteLocalRef(cls); // 新增：释放类引用，避免泄漏
}

// 关闭应用的函数 closeApp -> YxASVD
void YxASVD(JNIEnv* env) {
    // 自行实现这个玩意
}

// -------------------------- 修改原有JNI方法，使用公共函数 --------------------------
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_mine_util_GetString_getStringsFromKeyword(
    JNIEnv *env, 
    jobject thiz,
    jobject getString,
    jstring keyword_
) {
    // 1. Java String→C++ string
    const char *keyword = env->GetStringUTFChars(keyword_, nullptr);
    if (keyword == nullptr) return nullptr;

    // 2. 调用公共方法获取字符串
    std::vector<std::string> result = getDecryptedStrings(keyword);

    // 3. 释放资源
    env->ReleaseStringUTFChars(keyword_, keyword);
    if (result.empty()) return nullptr;

    // 4. 创建Java String数组返回（保持原有逻辑）
    jclass stringClass = env->FindClass(getDecryptedStrings("INFO")[5].c_str());
    if (stringClass == nullptr) return nullptr;
    jobjectArray resultArray = env->NewObjectArray(result.size(), stringClass, nullptr);
    env->DeleteLocalRef(stringClass);
    if (resultArray == nullptr) return nullptr;

    for (size_t i = 0; i < result.size(); ++i) {
        jstring str = env->NewStringUTF(result[i].c_str());
        env->SetObjectArrayElement(resultArray, i, str);
        env->DeleteLocalRef(str);
    }

    /* 5. 每调用10次或第一次调用时执行callJavaCopMethod
    // 闪退概率高，别这样干
    callCount++;
    if (callCount == 1 || callCount % 2 == 0) {
        callJavaCopMethod(env, getString);
    }
    */
    
    return resultArray;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_mine_layout_Fragment_HomeFragment_HohuLCAZvcOCyCbSAngk(
    JNIEnv *env, 
    jobject thiz
) {
    // 没啥用
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_mine_Service_ToolService_getNewTime(
    JNIEnv *env, 
    jobject thiz
) {

    // 获取当前时间戳
    auto duration = now.time_since_epoch();
    auto milliseconds = std::chrono::duration_cast<std::chrono::milliseconds>(duration);
    jlong currentTime = milliseconds.count();

    return currentTime;
}