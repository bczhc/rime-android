package pers.zhc.android.rime

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logging {
    private val logFileStream by lazy {
        File(MyApplication.CONTEXT.filesDir, "log").outputStream()
    }

    fun writeLog(line: String) {
        logFileStream.write("[${dateFormatter.format(Date())}] $line\n".toByteArray())
        logFileStream.flush()
    }

    private val dateFormatter by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
}
