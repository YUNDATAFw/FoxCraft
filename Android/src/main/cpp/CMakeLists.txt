cmake_minimum_required(VERSION 3.25.1)
project(MyProject)

# 设置构建类型为Release
set(CMAKE_BUILD_TYPE Release)

# 添加C和C++编译器的优化选项
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

# 添加共享库（将dex_verifier.cpp添加到m1库的源文件列表中）
add_library(m1 SHARED 
    native-lib.cpp 
)
add_library(m2 SHARED 
    rc4_decrypt.cpp 
)
add_library(m3 SHARED 
  dex_validate.cpp 
)

# 查找 log 库
find_library(log-lib log)
if(NOT log-lib)
    message(FATAL_ERROR "log library not found")
endif()

# 链接库
target_link_libraries(m1 ${log-lib})
target_link_libraries(m2 ${log-lib} m1)
target_link_libraries(m3 ${log-lib} m1)
