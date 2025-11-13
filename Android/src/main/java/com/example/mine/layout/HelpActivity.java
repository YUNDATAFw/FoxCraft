package com.example.mine.layout;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.noties.markwon.image.ImagesPlugin;;
import io.noties.markwon.Markwon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.example.mine.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView textView = findViewById(R.id.text_view);

        // 初始化 Markwon 并添加图片加载插件
        Markwon markwon = Markwon.builder(this)
                .usePlugin(ImagesPlugin.create()) // 添加图片加载插件
                .build();

        // 从 assets 文件夹中加载 readme.md 文件
        String markdownContent = loadMarkdownFromAssets("README.md");

        // 使用 Markwon 渲染 Markdown 内容
        markwon.setMarkdown(textView, markdownContent);
    }

    // 从 assets 文件夹中加载文件内容
    private String loadMarkdownFromAssets(String fileName) {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
