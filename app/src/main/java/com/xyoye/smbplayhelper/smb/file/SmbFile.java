package com.xyoye.smbplayhelper.smb.file;

import com.hierynomus.smbj.common.SMBRuntimeException;

import java.util.List;

/**
 * Created by xyoye on 2019/7/19.
 */

public interface SmbFile {
    String PATH_SEPARATOR = "\\";

    String getFileName();

    String getFilePath();

    SmbFile getParentFile();

    String getSmbPath();

    long getFileSize();

    boolean isExisting();

    boolean isDirectory();

    boolean isFile();

    List<SmbChildFile> listFile() throws SMBRuntimeException;

    boolean isSmbShareFile();
}
