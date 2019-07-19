package com.xyoye.smbplayhelper.smb.file;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import com.xyoye.smbplayhelper.smb.SmbConnectManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xyoye on 2019/7/15.
 */

public class SmbShareFile implements SmbFile{
    private Share share;
    private String fileName;

    public SmbShareFile(String shareName) {
        fileName = shareName;
        Session session = SmbConnectManager.getInstance().getSession();
        try {
            share = session.connectShare(shareName);
        }catch (Exception ignored){ }
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFilePath() {
        return fileName;
    }

    @Override
    public @Nullable SmbFile getParentFile() {
        return null;
    }

    @Override
    public String getSmbPath() {
        SmbPath smbPath = new SmbPath(
                SmbConnectManager.getInstance().getRootIP(),
                fileName);
        return smbPath.toUncPath();
    }

    @Override
    public long getFileSize() {
        return 0;
    }

    @Override
    public boolean isExisting() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    public @NonNull List<SmbChildFile> listFile() throws SMBRuntimeException {
        List<SmbChildFile> smbChildFileList = new ArrayList<>();
        if (share != null && share instanceof DiskShare){
            DiskShare diskShare = (DiskShare) share;
            for (FileIdBothDirectoryInformation item : diskShare.list("")) {
                if (item.getFileName().startsWith("."))
                    continue;
                smbChildFileList.add(new SmbChildFile(item.getFileName(), this, diskShare));
            }
            Collections.sort(smbChildFileList, (o1, o2) ->
                    o1.getFileName().compareTo(o2.getFileName()));
        }
        return smbChildFileList;
    }

    @Override
    public boolean isSmbShareFile() {
        return true;
    }
}
