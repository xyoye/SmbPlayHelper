package com.xyoye.libsmb_kotlin.controller

import com.xyoye.libsmb_kotlin.exception.SmbLinkException
import com.xyoye.libsmb_kotlin.info.SmbFileInfo
import com.xyoye.libsmb_kotlin.info.SmbLinkInfo
import java.io.InputStream

/**
 * Created by xyoye on 2019/12/25.
 */

interface Controller{
    fun linkStart(smbLinkInfo : SmbLinkInfo, exception: SmbLinkException): Boolean

    fun getParentList(): List<SmbFileInfo>

    fun getSelfList(): List<SmbFileInfo>

    fun getChildList(dirName: String): List<SmbFileInfo>

    fun getFileInputStream(fileName: String): InputStream?

    fun getFileLength(fileName: String): Long

    fun getCurrentPath(): String

    fun isRootDir(): Boolean

    fun release()
}