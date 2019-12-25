package com.xyoye.libsmb_kotlin.controller

import com.xyoye.jcifs_origin.smb.SmbException
import com.xyoye.jcifs_origin.smb.SmbFile
import com.xyoye.libsmb_kotlin.exception.SmbLinkException
import com.xyoye.libsmb_kotlin.info.SmbFileInfo
import com.xyoye.libsmb_kotlin.info.SmbLinkInfo
import com.xyoye.libsmb_kotlin.info.SmbType
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException

/**
 * Created by xyoye on 2019/12/25.
 */

class JCIFSController : Controller {
    companion object {
        private const val ROOT_FLAG = "/"
    }

    private var mPath = ""
    private var mAuthUrl = ""
    private var inputStream: InputStream? = null
    private val rootFileList = ArrayList<SmbFileInfo>()

    override fun linkStart(smbLinkInfo: SmbLinkInfo, exception: SmbLinkException): Boolean {
        mAuthUrl = "smb://" +
                if (smbLinkInfo.isAnonymous)
                    smbLinkInfo.IP
                else
                    smbLinkInfo.account + ":" + smbLinkInfo.password + "@" + smbLinkInfo.IP

        try {
            rootFileList.addAll(getFileInfoList(SmbFile(mAuthUrl).listFiles()))
            mPath = ROOT_FLAG
            return true
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            exception.addException(SmbType.JCIFS, e.message)
        }

        return false
    }

    override fun getParentList(): List<SmbFileInfo> {
        if (isRootDir())
            return emptyList()

        val fileInfoList = ArrayList<SmbFileInfo>()
        try {
            val parentPath = mPath.substring(0, mPath.length - 1)
            val index = parentPath.indexOf("/", 1)

            val endIndex = parentPath.lastIndexOf("/")
            mPath = mPath.substring(0, endIndex) + "/"

            if (index == -1)
                return rootFileList

            val smbFile = SmbFile(mAuthUrl + mPath)
            if (smbFile.isDirectory && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()))
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return fileInfoList
    }

    override fun getSelfList(): List<SmbFileInfo> {
        if (isRootDir())
            return rootFileList

        val fileInfoList = ArrayList<SmbFileInfo>()

        try {
            val smbFile = SmbFile(mAuthUrl + mPath)
            if (smbFile.isDirectory && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()))
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return fileInfoList
    }

    override fun getChildList(dirName: String): List<SmbFileInfo> {
        val fileInfoList = ArrayList<SmbFileInfo>()
        try {
            mPath += "$dirName/"
            val smbFile = SmbFile(mAuthUrl + mPath)
            if (smbFile.isDirectory && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()))
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return fileInfoList
    }

    override fun getFileInputStream(fileName: String): InputStream? {
        try {
            val filePath = "$mPath$fileName/"
            val smbFile = SmbFile(mAuthUrl + filePath)
            if (smbFile.isFile && smbFile.canRead()) {
                inputStream = smbFile.inputStream
                return inputStream
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getFileLength(fileName: String): Long {
        try {
            val filePath = "$mPath$fileName/"
            val smbFile = SmbFile(mAuthUrl + filePath)
            if (smbFile.isFile && smbFile.canRead()) {
                return smbFile.contentLength.toLong()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun getCurrentPath(): String {
        return if (mPath.length == 1) mPath else mPath.substring(0, mPath.length - 1)
    }

    override fun isRootDir(): Boolean {
        return ROOT_FLAG == mPath
    }

    override fun release() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFileInfoList(smbFiles: Array<SmbFile>): ArrayList<SmbFileInfo> {
        val fileInfoList = ArrayList<SmbFileInfo>()
        for (smbFile in smbFiles) {
            var isDirectory = false
            try {
                isDirectory = smbFile.isDirectory
            } catch (ignore: SmbException) {
            }

            var smbFileName = smbFile.name
            smbFileName =
                    if (smbFileName.endsWith("/"))
                        smbFileName.substring(0, smbFileName.length - 1)
                    else
                        smbFileName

            fileInfoList.add(SmbFileInfo(smbFileName, isDirectory))
        }
        return fileInfoList
    }


}