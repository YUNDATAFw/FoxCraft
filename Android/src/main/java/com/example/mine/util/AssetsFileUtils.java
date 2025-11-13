package com.example.mine.util;
import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AssetsFileUtils {
  public static String readFromAssets(Context context, String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        inputStream.close();
        return content.toString();
    }

}
