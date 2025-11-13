package com.example.mine.tool;

public class Max {
  private static boolean max = false;
  private static boolean max_xfc = false;
  //                            自动点击，自动放置，自动跳跃，实体崩服
  private static int[] useint = {80, 80, 80, 800};
  private static int[] maxint = {100, 100, 100, 1000};

  public static int[] get() {
    if (max) {
      return maxint;
    } else {
      return useint;
    }
  }

  public static int[] getInt() {
    return useint;
  }

  public static void setMax(boolean is) {
    Max.max = is;
  }

  public static boolean getMax() {
    return max;
  }

  public static void setMaxXfc(boolean is) {
    Max.max_xfc = is;
  }

  public static boolean getMaxXfc() {
    return max_xfc;
  }

  // 设置 useint 中特定位置的值
  public static void setUseintValue(int index, int value) {
    if (index >= 0 && index < useint.length) {
      useint[index] = value;
    } else {
      throw new IndexOutOfBoundsException("Index out of bounds for useint");
    }
  }

  // 设置 maxint 中特定位置的值
  public static void setMaxintValue(int index, int value) {
    if (index >= 0 && index < maxint.length) {
      maxint[index] = value;
    } else {
      throw new IndexOutOfBoundsException("Index out of bounds for maxint");
    }
  }

  // 获取 useint 的方法
  public static int[] getUseint() {
    return useint;
  }

  // 获取 maxint 的方法
  public static int[] getMaxint() {
    return maxint;
  }
}
