package com.example.mine.Item;

public class FunctionItem2 {
    private int iconResId;
    private String name;
    private Runnable action;
    private boolean isEnabled; // 添加开关状态
    private Runnable onEnableAction; // 开启事件
    private Runnable onDisableAction; // 关闭事件

    public FunctionItem2(int iconResId, String name, Runnable action) {
        this.iconResId = iconResId;
        this.name = name;
        this.action = action;
        this.isEnabled = false; // 默认关闭
    }

    public FunctionItem2(int iconResId, String name, Runnable action, Runnable onEnableAction, Runnable onDisableAction) {
        this.iconResId = iconResId;
        this.name = name;
        this.action = action;
        this.onEnableAction = onEnableAction;
        this.onDisableAction = onDisableAction;
        this.isEnabled = false; // 默认关闭
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getName() {
        return name;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Runnable getOnEnableAction() {
        return onEnableAction;
    }

    public Runnable getOnDisableAction() {
        return onDisableAction;
    }
}
