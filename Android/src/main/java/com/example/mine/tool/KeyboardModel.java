package com.example.mine.tool;

public class KeyboardModel {
    // 枚举类定义键盘模式
    public enum Mode {
        FULL_KEYBOARD, // 全键盘模式
        NON_FULL_KEYBOARD // 非全键盘模式
    }

    // 静态变量存储当前键盘模式
    private static Mode currentMode = Mode.FULL_KEYBOARD;

    // 设置当前键盘模式
    public static void setMode(Mode mode) {
        KeyboardModel.currentMode = mode;
    }

    // 获取当前键盘模式
    public static Mode getMode() {
        return currentMode;
    }
}
