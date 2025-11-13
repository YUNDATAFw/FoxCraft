package com.FoxPlugin;

import android.content.Context;
import android.util.Log;
import com.FoxPlugin.Functions;
import com.FoxPlugin.ITool;
import com.example.mine.tool.FPPlugin.Utils;
import com.example.mine.widget.StringToast;
import org.json.*;

public class Running {
  private Utils pluginUtils;
  private Context context;
  private ITool codeTool;

  public void run(Context context, String uuid, String uid, JSONArray code) {
    pluginUtils = new Utils();
    this.context = context;
    codeTool = new Functions();
    Log.d("fun", "定位：" + code.length());
    for (int i = 0; i < code.length(); i++) {
      try {
        JSONObject codeBlock = code.getJSONObject(i);
        String className = codeBlock.getString("class");
        switch (className) {
          case "IKeyIuputTool":
            {
              JSONObject inputCodeBlock = codeBlock.getJSONObject("Input");
              int rate = inputCodeBlock.getInt("rate");
              String key = inputCodeBlock.getString("key");
              String onKey = inputCodeBlock.getString("onKey");
              JSONArray fnKey = inputCodeBlock.getJSONArray("fnKey");
              int repeat = inputCodeBlock.getInt("repeat");
              codeTool.on_IKeyIuputTool(context, uuid, uid, i, rate, key, onKey, fnKey, repeat);
            }
            break;
          case "Toast":
            {
              String message = codeBlock.getString("message");
              String type = codeBlock.getString("type");
              int onPosition = StringToast.POSITION_BOTTOM;
              int duration = StringToast.DURATION_SHORT;
              int onType = StringToast.TYPE_INFO;
              switch (type) {
                case "info":
                  onType = StringToast.TYPE_INFO;
                  break;
                case "success":
                  onType = StringToast.TYPE_SUCCESS;
                  break;
                case "warning":
                  onType = StringToast.TYPE_WARNING;
                  break;
                case "error":
                  onType = StringToast.TYPE_ERROR;
                  break;
              }
              if (codeBlock.has("duration")) {
                duration = codeBlock.getInt("duration");
              }
              if (codeBlock.has("position")) {
                String position = codeBlock.getString("position");

                switch (position) {
                  case "top":
                    onPosition = StringToast.POSITION_TOP;
                    break;
                  case "bottom":
                    onPosition = StringToast.POSITION_BOTTOM;
                    break;
                  case "center":
                    onPosition = StringToast.POSITION_CENTER;
                    break;
                }
              }
              codeTool.on_Toast(context, message, duration, onPosition, onType);
            }
            break;
          case "Log":
            {
            }
            break;
        }
      } catch (JSONException err) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("错误出现在<%s/%s>").append("\n");
        errorMessage.append("codeLine:%d").append("\n");
        errorMessage.append("JSONException -> ").append("\n");
        errorMessage.append("%s").append("\n");
        pluginUtils.writeLogToPlugin(
            this.context, uuid, uid, errorMessage.toString(), uuid, uid, i, err.toString());
      }
    }
  }
}
