package com.xyoye.smbplayhelper.bean;

/**
 * Created by xyoye on 2019/7/23.
 */

public class SmbFileBean {
    private String smbFilePath;
    private String smbFileName;
    private boolean isDirectory;

    public String getSmbFilePath() {
        return smbFilePath;
    }

    public void setSmbFilePath(String smbFilePath) {
        this.smbFilePath = smbFilePath;
    }

    public String getSmbFileName() {
        return smbFileName;
    }

    public void setSmbFileName(String smbFileName) {
        this.smbFileName = smbFileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
