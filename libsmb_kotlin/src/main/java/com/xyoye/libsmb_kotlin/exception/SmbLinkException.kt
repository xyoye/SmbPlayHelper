package com.xyoye.libsmb_kotlin.exception

import com.xyoye.libsmb_kotlin.info.SmbType
import java.util.*

/**
 * Created by xyoye on 2019/12/25.
 */

class SmbLinkException : Exception() {
    var detailException = ArrayList<DetailException>()

    fun addException(smbType: SmbType, msg: String?) {
        detailException.add(DetailException(smbType, msg))
    }

    fun clearException() {
        detailException.clear()
    }

    fun getExceptionString(): String {
        val stringBuilder = StringBuilder()
        for (exception in detailException) {
            val typeName: String = exception.smbType.name
            stringBuilder
                    .append("\n")
                    .append(typeName)
                    .append("\n")
                    .append("Error: ")
                    .append(exception.errorMsg)
                    .append("\n")
        }
        return stringBuilder.toString()
    }

    data class DetailException(val smbType: SmbType, val errorMsg: String?)
}