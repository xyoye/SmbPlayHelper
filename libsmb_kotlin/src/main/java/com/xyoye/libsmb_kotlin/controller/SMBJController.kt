package com.xyoye.libsmb_kotlin.controller

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskEntry
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import com.xyoye.libsmb_kotlin.exception.SmbLinkException
import com.xyoye.libsmb_kotlin.info.SmbFileInfo
import com.xyoye.libsmb_kotlin.info.SmbLinkInfo
import com.xyoye.libsmb_kotlin.info.SmbType
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by xyoye on 2019/12/25.
 */

class SMBJController : Controller {

    private var rootFlag = "\\"
    private var mPath: String = ""
    private var diskShare: DiskShare? = null
    private var rootFileList = ArrayList<SmbFileInfo>()

    private var smbClient: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null
    private var inputStream: InputStream? = null


    override fun linkStart(smbLinkInfo: SmbLinkInfo, exception: SmbLinkException): Boolean {
        if (smbLinkInfo.rootFolder.isEmpty()) {
            exception.addException(SmbType.SMBJ, "Root Folder Must Not Empty")
            return false
        }

        val smbConfig = SmbConfig.builder()
                .withTimeout(5, TimeUnit.SECONDS)
                .withTimeout(5, TimeUnit.SECONDS)
                .withSoTimeout(5, TimeUnit.SECONDS)
                .build()

        try {
            smbClient = SMBClient(smbConfig)
            connection = smbClient?.connect(smbLinkInfo.IP)
            val authContext = AuthenticationContext(
                    smbLinkInfo.account, smbLinkInfo.password.toCharArray(), smbLinkInfo.domain)
            session = connection?.authenticate(authContext)

            rootFlag += smbLinkInfo.rootFolder
            mPath = rootFlag

            diskShare = session?.connectShare(smbLinkInfo.rootFolder) as DiskShare

            rootFileList = getFileInfoList(diskShare!!.list(""), diskShare!!)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            exception.addException(SmbType.SMBJ, e.message)
        }
        return false
    }

    override fun getParentList(): List<SmbFileInfo> {
        if (isRootDir())
            return emptyList()

        val startIndex = mPath.indexOf("\\", 1) + 1
        val endIndex = mPath.lastIndexOf("\\")

        val parentPath = if (startIndex < endIndex) mPath.substring(startIndex, endIndex) else ""

        mPath = mPath.substring(0, endIndex)

        val infoList = ArrayList<SmbFileInfo>()
        try {
            val parentDir = openDirectory(diskShare!!, parentPath)
            infoList.addAll(getFileInfoList(parentDir.list(), diskShare!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return infoList
    }

    override fun getSelfList(): List<SmbFileInfo> {
        if (isRootDir())
            return rootFileList

        val infoList = ArrayList<SmbFileInfo>()
        try {
            val selfDir = openDirectory(diskShare!!, getPathNotShare(""))
            infoList.addAll(getFileInfoList(selfDir.list(), diskShare!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return infoList
    }

    override fun getChildList(dirName: String): List<SmbFileInfo> {
        val infoList = ArrayList<SmbFileInfo>()
        try {
            val childDIr = openDirectory(diskShare!!, getPathNotShare(dirName))
            mPath += "\\" + dirName
            infoList.addAll(getFileInfoList(childDIr.list(), diskShare!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return infoList
    }

    override fun getFileInputStream(fileName: String): InputStream? {
        val filePath = getPathNotShare(fileName)
        try {
            val file = openFile(diskShare!!, filePath)
            inputStream = file.inputStream
            return inputStream
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun getFileLength(fileName: String): Long {
        val filePath = getPathNotShare(fileName)

        try {
            val file = openFile(diskShare!!, filePath)
            val standardInfo = file.getFileInformation(FileStandardInformation::class.java)
            return standardInfo.endOfFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    override fun getCurrentPath(): String {
        return if (mPath.isEmpty()) "/" else mPath.replace("\\", "/")
    }

    override fun isRootDir(): Boolean = rootFlag == mPath

    override fun release() {
        try {
            inputStream?.close()
            diskShare?.close()
            session?.close()
            connection?.close()
            smbClient?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFileInfoList(infoList: List<FileIdBothDirectoryInformation>, diskShare: DiskShare): ArrayList<SmbFileInfo> {
        val fileInfoList = ArrayList<SmbFileInfo>()
        for (dirInfo in infoList) {
            if (dirInfo.fileName.startsWith("."))
                continue

            val diskEntry = openDiskEntry(diskShare, getPathNotShare(dirInfo.fileName))
            val standardInfo = diskEntry.getFileInformation(FileStandardInformation::class.java)
            fileInfoList.add(SmbFileInfo(dirInfo.fileName, standardInfo.isDirectory))
        }
        return fileInfoList
    }

    private fun getPathNotShare(filePath: String): String {
        val index = mPath.indexOf("\\", 1)
        return if (index == -1) {
            filePath
        } else {
            val fileName = if (filePath.isNotEmpty()) ("\\" + filePath) else ""
            mPath.substring(index + 1) + fileName
        }
    }

    private fun openFile(diskShare: DiskShare, fileName: String): File = diskShare.openFile(
            fileName,
            EnumSet.of(AccessMask.FILE_READ_DATA),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
    )

    private fun openDirectory(diskShare: DiskShare, filePath: String): Directory = diskShare.openDirectory(
            filePath,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
    )

    private fun openDiskEntry(diskShare: DiskShare, filePath: String): DiskEntry = diskShare.open(
            filePath,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
    )
}