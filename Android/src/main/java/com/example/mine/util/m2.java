package com.example.mine.util;
import android.content.Context;

public class m2 {
    static {
        System.loadLibrary("m2");
    }

    public static native String m2d(Context context,byte[] key, String base64Ciphertext);
}
