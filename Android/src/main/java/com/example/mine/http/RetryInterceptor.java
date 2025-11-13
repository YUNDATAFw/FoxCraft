package com.example.mine.http;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    // 最大重试次数（含首次请求，总次数 = 重试次数 + 1）
    private final int maxRetryCount;
    // 已重试次数
    private int retryCount = 0;

    public RetryInterceptor(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        // 循环重试，直到达到最大次数
        while (retryCount <= maxRetryCount) {
            try {
                response = chain.proceed(request);
                // 如果响应成功（2xx）或不需要重试的状态码，直接返回
                if (response.isSuccessful() || !shouldRetry(response.code())) {
                    return response;
                }
                // 若需要重试，关闭当前响应（避免连接泄漏）
                response.close();
            } catch (IOException e) {
                lastException = e;
                // 捕获连接异常，准备重试
            }

            retryCount++;
            // 可选：添加重试延迟（如指数退避）
            if (retryCount <= maxRetryCount) {
                try {
                    Thread.sleep(1000 * retryCount); // 延迟 1s, 2s, 3s...
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 达到最大重试次数仍失败，抛出最后一次异常
        throw lastException != null ? lastException : new IOException("Max retry count reached");
    }

    // 定义需要重试的状态码（如 5xx 服务器错误）
    private boolean shouldRetry(int code) {
        return code >= 500 && code < 600;
    }
}