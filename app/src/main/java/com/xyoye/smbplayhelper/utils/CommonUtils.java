package com.xyoye.smbplayhelper.utils;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by xyoye on 2019/7/23.
 */

public class CommonUtils {

    /**
     * 判断视频格式
     */
    public static boolean isMediaFile(String fileName){
        switch (getFileExtension(fileName).toLowerCase()){
            case "3gp":
            case "avi":
            case "flv":
            case "mp4":
            case "m4v":
            case "mkv":
            case "mov":
            case "mpeg":
            case "mpg":
            case "mpe":
            case "rm":
            case "rmvb":
            case "wmv":
            case "asf":
            case "asx":
            case "dat":
            case "vob":
            case "m3u8":
                return true;
            default: return false;
        }
    }
    /**
     * 获取文件格式
     */
    public static String getFileExtension(final String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

}
