package com.example.imagepreviewertc.utils

import android.content.Context
import android.os.Environment
import java.io.File

fun generateEditFile(context: Context): File? {
    return getEmptyFile(
        EDIT_FILE_PREFIX
                + System.currentTimeMillis() + ".jpg",
        context
    )
}

private fun getEmptyFile(name: String?, context: Context): File? {
    val folder: File? = createFolders(context)
    if (folder != null) {
        if (folder.exists()) {
            return File(folder, name)
        }
    }
    return null
}

private fun createFolders(context: Context): File? {
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    val cacheFolder = File(baseDir, FOLDER_NAME)
    if (cacheFolder.exists()) return cacheFolder
    if (cacheFolder.isFile) cacheFolder.delete()
    return if (cacheFolder.mkdirs()) cacheFolder else context.filesDir
}