package com.xyoye.libsmb.info;

/**
 * Created by xyoye on 2019/12/20.
 */

public enum  SmbType {
    JCIFS,

    JCIFS_NG,

    SMBJ,

    SMBJ_RPC;

    public static String getTypeName(SmbType type){
        switch (type){
            case SMBJ:
                return "SMBJ";
            case JCIFS:
                return "JCIFS";
            case JCIFS_NG:
                return "JCIFS_NG";
            case SMBJ_RPC:
                return "SMBJ_RPC";
            default:
                return "";
        }
    }
}
