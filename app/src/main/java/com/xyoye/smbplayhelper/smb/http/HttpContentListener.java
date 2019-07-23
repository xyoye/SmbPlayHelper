package com.xyoye.smbplayhelper.smb.http;

import java.io.InputStream;

/**
 * Created by xyoye on 2019/7/19.
 */

public interface HttpContentListener {
    /**
     * 获取视频流
     */
    InputStream getContentInputStream();

    /**
     * 获取视频类型
     */
    String getContentType();

    /**
     * 获取视频长度
     */
    long getContentLength();
}
