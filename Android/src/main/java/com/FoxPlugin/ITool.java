package com.FoxPlugin;
import android.content.Context;
import org.json.JSONArray;

public interface ITool {
  void on_IKeyIuputTool(Context context,String uuid,String uid,int line,int rate,String key, String onKey, JSONArray fnKey,int repeat);
  void on_Toast(Context context,String message, int duration, int position, int type);
  void on_Log();
}
