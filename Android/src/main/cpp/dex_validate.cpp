#include <jni.h>
#include <cstdlib>
#include "native-lib.h"

// 关闭应用的函数 closeApp -> z
void z(JNIEnv* env) {
    // 自行解决退出app的逻辑
}

extern "C" JNIEXPORT void JNICALL 
Java_com_example_mine_util_opo_lol(JNIEnv* env, jobject /* this */) {
    z(env);
}

extern "C" JNIEXPORT void JNICALL 
Java_com_example_mine_util_opo_iop(JNIEnv* env, jobject /* this */,jobject context) {
    
}
