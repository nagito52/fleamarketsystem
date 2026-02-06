package com.example.fleamarketsystem.util;

public class CloudinaryUtils {
    
    // staticメソッドにすることで、newせずにどこからでも呼び出せるようにします
    public static String extractPublicId(String url) {
        if (url == null || !url.contains("/")) {
            return null;
        }
        // 最後のスラッシュ以降（ファイル名）を取得し、拡張子を除くロジック
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }
}