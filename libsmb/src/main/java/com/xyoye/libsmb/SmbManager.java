package com.xyoye.libsmb;

import com.xyoye.libsmb.controller.Controller;
import com.xyoye.libsmb.controller.JCIFSController;
import com.xyoye.libsmb.controller.JCIFS_NGController;
import com.xyoye.libsmb.controller.SMBJController;
import com.xyoye.libsmb.controller.SMBJ_RPCController;
import com.xyoye.libsmb.exception.SmbLinkException;
import com.xyoye.libsmb.info.SmbLinkInfo;
import com.xyoye.libsmb.info.SmbType;
import com.xyoye.libsmb.utils.SmbUtils;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbManager {
    private SmbType mSmbType;
    private boolean isLinked;
    private Controller controller;
    private SmbLinkException smbLinkException;

    private boolean smbJRPCEnable = true;
    private boolean smbJEnable = true;
    private boolean jcifsNGEnable = true;
    private boolean jcifsEnable = true;

    private static class Holder {
        static SmbManager instance = new SmbManager();
    }

    private SmbManager() {
        smbLinkException = new SmbLinkException();
    }

    public static SmbManager getInstance() {
        return Holder.instance;
    }

    /**
     * link to the smb server from smbV2 to smbV1
     *
     * @param smbLinkInfo link data
     */
    public boolean linkStart(SmbLinkInfo smbLinkInfo) {

        smbLinkException.clearException();

        if (!smbLinkInfo.isAnonymous()) {
            if (SmbUtils.containsEmptyText(smbLinkInfo.getAccount(), smbLinkInfo.getAccount())) {
                throw new NullPointerException("Account And Password Must NotNull");
            }
        }

        //SMB V2
        isLinked = true;

        if (jcifsNGEnable) {
            controller = new JCIFS_NGController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS_NG;
                return true;
            }
        }

        if (smbJRPCEnable) {
            controller = new SMBJ_RPCController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ_RPC;
                return true;
            }
        }

        if (smbJEnable && !SmbUtils.isTextEmpty(smbLinkInfo.getRootFolder())) {
            controller = new SMBJController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ;
                return true;
            }
        }

        //SMB V1
        if (jcifsEnable) {
            controller = new JCIFSController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS;
                return true;
            }
        }

        isLinked = false;
        return false;
    }

    /**
     * get smb tools type
     */
    public String getSmbType() {
        return mSmbType == null ? "" : SmbType.getTypeName(mSmbType);
    }

    /**
     * is the link successful
     */
    public boolean isLinked() {
        return isLinked;
    }

    /**
     * get link controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * link error info
     */
    public SmbLinkException getException() {
        return smbLinkException;
    }

    public void setEnable(boolean jcifsNGEnable, boolean smbJRPCEnable, boolean smbJEnable, boolean jcifsEnable) {
        this.jcifsNGEnable = jcifsNGEnable;
        this.smbJRPCEnable = smbJRPCEnable;
        this.smbJEnable = smbJEnable;
        this.jcifsEnable = jcifsEnable;
    }
}
