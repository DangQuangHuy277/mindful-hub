package io.github.dangquanghuy277.mindfulhub.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A6: every table, as one SQLite file in Downloads, readable with `sqlite3`, DB Browser or pandas.
 * `VACUUM INTO` writes a consistent copy while the gate keeps writing; Downloads needs no
 * permission on Android 10+ and survives uninstalling the app.
 */
object DataExport {
    /** Returns the file name in Downloads. */
    suspend fun toDownloads(context: Context, db: MindfulDatabase): String = withContext(Dispatchers.IO) {
        val name = "mindful-hub-" + LocalDateTime.now().format(STAMP) + ".db"
        val tmp = File(context.cacheDir, name)
        tmp.delete() // VACUUM INTO refuses an existing file
        try {
            db.openHelper.writableDatabase.execSQL("VACUUM INTO ?", arrayOf(tmp.path))
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.sqlite3")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: error("Downloads refused the file")
            resolver.openOutputStream(uri).use { out -> tmp.inputStream().use { it.copyTo(out ?: error("no output stream")) } }
            name
        } finally {
            tmp.delete()
        }
    }

    private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")
}
