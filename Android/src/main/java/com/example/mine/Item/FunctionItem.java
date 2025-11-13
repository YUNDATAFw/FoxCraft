package com.example.mine.Item;

public class FunctionItem {
    private int iconResId; // 图标的资源ID
    private String title;  // 标题
    private Runnable onClickAction; // 点击时执行的代码块

    public FunctionItem(int iconResId, String title, Runnable onClickAction) {
        this.iconResId = iconResId;
        this.title = title;
        this.onClickAction = onClickAction;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public Runnable getOnClickAction() {
        return onClickAction;
    }
}
