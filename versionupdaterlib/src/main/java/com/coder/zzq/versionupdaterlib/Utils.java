package com.coder.zzq.versionupdaterlib;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.coder.zzq.versionupdaterlib.bean.DownloadFileInfo;
import com.coder.zzq.versionupdaterlib.bean.OldDownloadInfo;

import java.io.File;

/**
 * Created by pig on 2018/1/24.
 */

public class Utils {

    public static DownloadManager getDownloadManager(Context context) {
        return (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static DownloadFileInfo getInfoOfDownloadFile(Context context, long downloadId) {

        DownloadManager downloadManager = getDownloadManager(context);

        DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                downloadFileInfo.setDownloadStatus(status);
                String uriStr = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                if (!TextUtils.isEmpty(uriStr)) {
                    downloadFileInfo.setUri(Uri.parse(uriStr));
                }
                int sizeBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                downloadFileInfo.setFileSizeBytes(sizeBytes);

                int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                downloadFileInfo.setReason(reason);

            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return downloadFileInfo;
    }


    public static void installApk(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.FILE_PROVIDER_AUTHORITIES, new File(uri.getEncodedPath()));
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }


    public static String checkNullOrEmpty(String str) {

        if (str == null || str.trim().length() == 0) {
            throw new IllegalArgumentException("参数字符串不可为null或者空白！");
        }

        return str;
    }


    public static final String REMOTE_VERSION_INFO_PREF = "remote_version_info";
    public static final String VERSION_INFO = "version_info";


    private static SharedPreferences remoteVersionInfoPref(Context context) {
        return context.getSharedPreferences(REMOTE_VERSION_INFO_PREF, Context.MODE_PRIVATE);
    }

    public static void storeOldDownloadInfo(Context context, long downloadId, int versionCode) {
        OldDownloadInfo versionInfo = new OldDownloadInfo(downloadId, versionCode);
        remoteVersionInfoPref(context).edit().putString(VERSION_INFO, versionInfo.toString()).commit();
    }

    public static OldDownloadInfo fetchOldDownloadInfo(Context context) {
        String jsonStr = remoteVersionInfoPref(context).getString(VERSION_INFO, null);
        return jsonStr == null ? null : new OldDownloadInfo(jsonStr);
    }

    public static void clearStoredOldDownloadInfo(Context context) {
        remoteVersionInfoPref(context).edit().putString(VERSION_INFO, null).commit();
    }


    public static int localVersionCode(Context context) {
        int versionCode = 1;

        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

}
