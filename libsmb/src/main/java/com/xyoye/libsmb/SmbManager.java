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
    private LinkCallback linkCallback;

    private boolean smbJRPCEnable = true;
    private boolean smbJEnable = true;
    private boolean jcifsNGEnable = true;
    private boolean jcifsEnable = true;

    public static class Holder {
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
    public void linkStart(SmbLinkInfo smbLinkInfo) {

        smbLinkException.clearException();

        if (!smbLinkInfo.isAnonymous()) {
            if (SmbUtils.containsEmptyText(smbLinkInfo.getAccount(), smbLinkInfo.getAccount())) {
                throw new NullPointerException("Account And Password Must NotNull");
            }
        }

        //SMB V2
        isLinked = true;

        if (smbJRPCEnable) {
            if (linkCallback != null)
                linkCallback.onLinkChange("SMBJ_RPC");
            controller = new SMBJ_RPCController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ_RPC;
                if (linkCallback != null)
                    linkCallback.onSuccess();
                return;
            }
        }

        if (jcifsNGEnable) {
            if (linkCallback != null)
                linkCallback.onLinkChange("JCIFS_NG");
            controller = new JCIFS_NGController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS_NG;
                if (linkCallback != null)
                    linkCallback.onSuccess();
                return;
            }
        }

        if (smbJEnable && !SmbUtils.isTextEmpty(smbLinkInfo.getRootFolder())) {
            if (linkCallback != null)
                linkCallback.onLinkChange("SMBJ");
            controller = new SMBJController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ;
                if (linkCallback != null)
                    linkCallback.onSuccess();
                return;
            }
        }

        //SMB V1
        if (jcifsEnable) {
            if (linkCallback != null)
                linkCallback.onLinkChange("JCIFS");
            controller = new JCIFSController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS;
                if (linkCallback != null)
                    linkCallback.onSuccess();
                return;
            }
        }

        isLinked = false;
        linkCallback.onFailed();
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

    public void setEnable(boolean smbJRPCEnable, boolean smbJEnable, boolean jcifsNGEnable, boolean jcifsEnable) {
        this.smbJRPCEnable = smbJRPCEnable;
        this.smbJEnable = smbJEnable;
        this.jcifsNGEnable = jcifsNGEnable;
        this.jcifsEnable = jcifsEnable;
    }

    public interface LinkCallback {
        void onLinkChange(String type);

        void onSuccess();

        void onFailed();
    }

    public void setLinkCallback(LinkCallback callback) {
        this.linkCallback = callback;
    }
}
