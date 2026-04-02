package com.qh.foreupdatemanager.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * UpdateApkTool
 * 下载方法类
 */
public class UpdateApkTool {
    // 下载取消标志
    private static  volatile  boolean isCancelled  = false;
    /**
     * cancelDownLoad()
     * 取消下载
     */
    public static  void cancelDownLoad (){
        isCancelled = true;
    }
    /**
     * downLoadApkFromUrl()
     * APP下载
     *（url地址， 下载文件， 监听回调 ）
     */
    public static void downLoadApkFromUrl(String urlStr, File destinationFile, ProgressListener listener) {
        isCancelled = false;
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("Download", "服务器返回错误" + responseCode);
                    return;
                }
                long totalSize = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    totalSize = connection.getContentLengthLong();
                }
                else {
                    totalSize = connection.getContentLength(); // < API 24, 可能为 int 最大值
                }
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(destinationFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long downloaded = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if(isCancelled){
                        outputStream.close();
                        inputStream.close();
                        destinationFile.delete(); // 删除未下载完的文件
                        return ;
                    }
                    outputStream.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;
                    if (listener != null && totalSize > 0) {
                        int progress = (int) ((downloaded * 100) / totalSize);
                        listener.onProgress(progress);
                    }
                }
                outputStream.close();
                inputStream.close();
                if (listener != null) listener.onCompleted(destinationFile);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) listener.onError(e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
    /**
     * 监听URL这个地址是否存在
     * isUrlExists()
     * (url 地址， 监听回调)
     */
    public static void isUrlExists(String urlStr, ExistsListener listener) {
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int responseCode = connection.getResponseCode();
                System.out.println("responseCode :: " + responseCode);

                Handler mainHandler = new Handler(Looper.getMainLooper());
                if (responseCode >= 200 && responseCode < 400) {
                    mainHandler.post(listener::onCompleted);
                } else {
                    mainHandler.post(listener::onFailed);
                }
            } catch (Exception e) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(listener::onFailed);
            }
        }).start();
    }
    @SuppressLint("ObsoleteSdkInt")
    public static void installApk(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            Log.e("InstallApk", "文件不存在");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }/**
     * 版本号比较工具
     * @return 1: v1 > v2; -1: v1 < v2; 0: 相等
     */
    public static int compareVersion(String v1, String v2) {
        if (v1 == null || v2 == null) return 0;
        // 使用正则表达式清理掉非数字和非点的字符（比如去掉 "V"）
        String cleanV1 = v1.replaceAll("[^\\d.]", "");
        String cleanV2 = v2.replaceAll("[^\\d.]", "");
        String[] nodes1 = cleanV1.split("\\.");
        String[] nodes2 = cleanV2.split("\\.");
        int length = Math.max(nodes1.length, nodes2.length);
        for (int i = 0; i < length; i++) {
            // 增加容错：如果清理后某一段为空，则视为 0
            int p1 = 0;
            if (i < nodes1.length && !nodes1[i].isEmpty()) {
                p1 = Integer.parseInt(nodes1[i]);
            }
            int p2 = 0;
            if (i < nodes2.length && !nodes2[i].isEmpty()) {
                p2 = Integer.parseInt(nodes2[i]);
            }
            if (p1 > p2) return 1;
            if (p1 < p2) return -1;
        }
        return 0;
    }
    public interface ProgressListener {
        void onProgress(int percent);
        void onCompleted(File file);
        void onError(Exception e);
    }

    public interface ExistsListener{
        void onCompleted();
        void onFailed();
    }
}

