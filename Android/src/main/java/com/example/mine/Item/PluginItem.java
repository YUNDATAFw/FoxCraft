package com.example.mine.Item;

public class PluginItem {
  private String name;
  private String version;
  private String description;
  private String uuid;

  public PluginItem(String uuid, String name, String version, String description) {
    this.name = name;
    this.version = version;
    this.description = description;
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getDescription() {
    return description;
  }

  public String getUUID() {
    return this.uuid;
  }
}
