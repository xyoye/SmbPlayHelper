package com.xyoye.libsmb_kotlin.controller

import com.xyoye.libsmb_kotlin.exception.SmbLinkException
import com.xyoye.libsmb_kotlin.info.SmbFileInfo
import com.xyoye.libsmb_kotlin.info.SmbLinkInfo
import com.xyoye.libsmb_kotlin.info.SmbType
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by xyoye on 2019/12/25.
 */

class JCIFS_NGController : Controller {
    companion object {
        private const val ROOT_FLAG = "/"
    }

    private var mPath = ""
    private var mAuthUrl: String? = null
    private var cifsContext: CIFSContext? = null
    private var inputStream: InputStream? = null

    private var rootFileList = ArrayList<SmbFileInfo>()

    override fun linkStart(smbLinkInfo: SmbLinkInfo, exception: SmbLinkException): Boolean {
        try {
            mAuthUrl = "smb://" +
                    if (smbLinkInfo.isAnonymous)
                        smbLinkInfo.IP
                    else
                        smbLinkInfo.account + ":" + smbLinkInfo.password + "@" + smbLinkInfo.IP
            val authenticator = NtlmPasswordAuthenticator(smbLinkInfo.domain, smbLinkInfo.account, smbLinkInfo.password)

            val properties = Properties()
            properties.setProperty("jcifs.smb.client.responseTimeout", "5000")
            val configuration = PropertyConfiguration(properties)

            cifsContext = BaseContext(configuration).withCredentials(authenticator)
            val address = cifsContext!!.nameServiceClient.getByName(smbLinkInfo.IP)

            cifsContext!!.transportPool.logon(cifsContext, address)

            val rootFile = SmbFile(mAuthUrl, cifsContext)

            rootFileList.clear()
            rootFileList.addAll(getFileInfoList(rootFile.listFiles()))

            mPath = ROOT_FLAG

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            exception.addException(SmbType.JCIFS_NG, e.message)
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

            val smbFile = SmbFile(mAuthUrl + mPath, cifsContext)
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
            val smbFile = SmbFile(mAuthUrl + mPath, cifsContext)
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
            val smbFile = SmbFile(mAuthUrl + mPath, cifsContext)
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
            val smbFile = SmbFile(mAuthUrl + filePath, cifsContext)
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
            val smbFile = SmbFile(mAuthUrl + filePath, cifsContext)
            if (smbFile.isFile && smbFile.canRead()) {
                return smbFile.contentLengthLong
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
            cifsContext?.close()
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