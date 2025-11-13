package com.example.mine.util;

import android.content.pm.PackageManager
import android.content.Context

class AppInfo(private val context: Context) {

    /**
     * 获取应用名称
     */
    fun getAppName(): String {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    /**
     * 获取应用版本号
     */
    fun getAppVersionName(): String {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    }

    /**
     * 获取应用版本代码
     */
    fun getAppVersionCode(): Long {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.longVersionCode
    }
}
