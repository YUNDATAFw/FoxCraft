package com.example.mine.util;

import android.content.Context;
import com.example.mine.tool.StatusTool;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class GetString {
  // 全局静态变量用于保存Context
  private static Context globalContext;

  // 加载动态库
  static {
    System.loadLibrary("m1");
  }

  // 定义本地方法
  private native String[] getStringsFromKeyword(GetString getString, String keyword);

  // 修改getStringsFromKeyword方法，保存Context到全局静态变量
  public String[] getStringsFromKeywordWrapper(
      Context context, GetString getString, String keyword) {
    // 保存Context到全局静态变量
    globalContext = context;
    // 调用原生方法
    return getStringsFromKeyword(getString, keyword);
  }

  public void cop() {
    // 没啥用
}
