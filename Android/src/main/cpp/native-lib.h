#ifndef NATIVE_LIB_H
#define NATIVE_LIB_H

#include <string>
#include <vector>

// 声明函数原型，让其他文件可以调用
std::vector<std::string> getDecryptedStrings(const std::string& keyword);

#endif // NATIVE_LIB_H