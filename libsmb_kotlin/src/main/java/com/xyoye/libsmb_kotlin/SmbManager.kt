package com.xyoye.libsmb_kotlin

import com.xyoye.libsmb_kotlin.controller.*
import com.xyoye.libsmb_kotlin.exception.SmbLinkException
import com.xyoye.libsmb_kotlin.info.SmbLinkInfo
import com.xyoye.libsmb_kotlin.info.SmbType

/**
 * Created by xyoye on 2019/12/25.
 */

class SmbManager private constructor() {

    companion object {
        fun getInstance(): SmbManager {
            return Holder.instance
        }
    }

    private object Holder {
        val instance = SmbManager()
    }

    var isLinked = false
    var controller: Controller? = null
    var smbLinkException = SmbLinkException()

    var smbJRPCEnable = false
    var smbJEnable = false
    var jcifsNgEnable = false
    var jcifsEnable = false

    private var mSmbType: SmbType? = null
    private var linkCallback: LinkCallback? = null

    fun linkStart(smbLinkInfo: SmbLinkInfo) {
        smbLinkException.clearException()

        if (!smbLinkInfo.isAnonymous) {
            if (smbLinkInfo.account.isEmpty() || smbLinkInfo.password.isEmpty()) {
                throw NullPointerException("Account And Password Must Not Null")
            }
        }

        isLinked = true
        if (smbJRPCEnable) {
            linkCallback?.onLinkChange("SMBJ_RPC")
            controller = SMBJ_RPCController()
            if (controller!!.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ_RPC
                linkCallback?.onSuccess()
                return
            }
        }

        if (jcifsNgEnable) {
            linkCallback?.onLinkChange("JCIFS_NG")
            controller = JCIFS_NGController()
            if (controller!!.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS_NG
                linkCallback?.onSuccess()
                return
            }
        }

        if (smbJEnable && smbLinkInfo.rootFolder.isNotEmpty()) {
            linkCallback?.onLinkChange("SMBJ")
            controller = SMBJController()
            if (controller!!.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ
                linkCallback?.onSuccess()
                return
            }
        }

        if (jcifsEnable) {
            linkCallback?.onLinkChange("JCIFS")
            controller = JCIFSController()
            if (controller!!.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS
                linkCallback?.onSuccess()
                return
            }
        }

        isLinked = false
        linkCallback?.onFailed()
    }

    fun getSmbType(): String {
        return if (mSmbType == null) "" else mSmbType!!.name
    }

    fun setEnable(smbJRPCEnable: Boolean, smbJEnable: Boolean, jcifsNgEnable:Boolean, jcifsEnable: Boolean){
        this.smbJRPCEnable = smbJRPCEnable
        this.smbJEnable = smbJEnable
        this.jcifsNgEnable = jcifsNgEnable
        this.jcifsEnable = jcifsEnable
    }

    fun setLinkCallback(linkCallback: LinkCallback){
        this.linkCallback = linkCallback
    }

    interface LinkCallback {
        fun onLinkChange(type: String)

        fun onSuccess()

        fun onFailed()
    }
}