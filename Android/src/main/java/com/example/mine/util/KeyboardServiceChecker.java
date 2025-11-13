package com.example.mine.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class KeyboardServiceChecker {

    /**
     * 检查指定的键盘服务是否被勾选
     *
     * @param context     应用上下文
     * @param serviceClassName 键盘服务的类名（例如："com.example.keyboard.MyKeyboardService"）
     * @return 如果键盘服务被勾选，返回true；否则返回false
     */
    public static boolean isKeyboardServiceEnabled(Context context, String serviceClassName) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return false;
        }

        // 获取当前设备的所有输入法服务
        List<InputMethodInfo> enabledInputMethods = inputMethodManager.getEnabledInputMethodList();
        if (enabledInputMethods == null) {
            return false;
        }

        // 遍历输入法服务列表，检查指定的键盘服务是否在其中
        for (InputMethodInfo inputMethodInfo : enabledInputMethods) {
            if (serviceClassName.equals(inputMethodInfo.getServiceName())) {
                return true;
            }
        }

        return false;
    }
  
}
