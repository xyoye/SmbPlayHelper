package com.xyoye.smbplayhelper.smb.file;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.xyoye.smbplayhelper.smb.SmbConnectManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by xyoye on 2019/7/15.
 */

public class SmbChildFile implements SmbFile{
    private SmbShareFile shareFile;
    private DiskShare diskShare;
    private String filePath;

    public SmbChildFile(@NonNull String smbUrl, @NonNull SmbShareFile shareFile, @Nullable DiskShare diskShare){
        this.filePath = smbUrl;
        this.shareFile = shareFile;
        this.diskShare = diskShare;
    }

    @Override
    public String getFileName() {
        int lastIndex = filePath.lastIndexOf(PATH_SEPARATOR);
        return filePath.substring(lastIndex + 1);
    }

    @Override
    public long getFileSize() {
        FileAllInformation fileAllInformation = diskShare.getFileInformation(filePath);
        FileStandardInformation fileStandardInformation = fileAllInformation.getStandardInformation();
        return fileStandardInformation.getEndOfFile();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public SmbFile getParentFile() {
        int lastIndex = filePath.lastIndexOf(PATH_SEPARATOR);
        if (lastIndex == 0 || lastIndex == -1) {
            return shareFile;
        } else {
            return new SmbChildFile(filePath.substring(0, lastIndex), shareFile, diskShare);
        }
    }

    @Override
    public String getSmbPath() {
        SmbPath smbPath = new SmbPath(
                SmbConnectManager.getInstance().getRootIP(),
                diskShare.getSmbPath().getShareName(),
                filePath);
        return smbPath.toUncPath();
    }

    @Override
    public boolean isExisting() {
        return isDirectory() || isFile();
    }

    @Override
    public boolean isDirectory() {
        return diskShare.folderExists(filePath);
    }

    @Override
    public boolean isFile() {
        return diskShare.fileExists(filePath);
    }

    @Override
    public boolean isSmbShareFile() {
        return false;
    }

    @Override
    public @Nullable List<SmbChildFile> listFile() throws SMBRuntimeException {
        if (isFile()) return null;
        List<SmbChildFile> childFileList = new ArrayList<>();
        for (FileIdBothDirectoryInformation item : diskShare.list(filePath)) {
            if (item.getFileName().startsWith("."))
                continue;
            String separator = TextUtils.isEmpty(filePath) ? "" : PATH_SEPARATOR;
            String smbUrl = filePath + separator + item.getFileName();
            childFileList.add(new SmbChildFile(smbUrl, shareFile, diskShare));
        }
        Collections.sort(childFileList, (o1, o2) -> o1.getFileName().compareTo(o2.getFileName()));
        return childFileList;
    }


    public InputStream getInputStream() {
        File file = diskShare.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN, null);
        return file.getInputStream();
    }

    public OutputStream getOutputStream(boolean appendContent) {
        SMB2CreateDisposition mode = !appendContent
                ? SMB2CreateDisposition.FILE_OVERWRITE_IF
                : SMB2CreateDisposition.FILE_OPEN_IF;
        File file = diskShare.openFile(filePath, EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL,
                mode, null);
        return file.getOutputStream(appendContent);
    }
}
