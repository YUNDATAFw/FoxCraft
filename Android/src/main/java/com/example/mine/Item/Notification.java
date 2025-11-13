package com.example.mine.Item;

public class Notification {
    private String title;
    private String content;
    private boolean isExpanded;

    public Notification(String title, String content) {
        this.title = title;
        this.content = content;
        this.isExpanded = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
