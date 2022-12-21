package io.element.android.x.core.file

import timber.log.Timber
import java.io.File
import java.util.zip.GZIPOutputStream

/**
 * GZip a file.
 *
 * @param file the input file
 * @return the gzipped file
 */
fun compressFile(file: File): File? {
    Timber.v("## compressFile() : compress ${file.name}")

    val dstFile = file.resolveSibling(file.name + ".gz")

    if (dstFile.exists()) {
        dstFile.delete()
    }

    return try {
        GZIPOutputStream(dstFile.outputStream()).use { gos ->
            file.inputStream().use {
                it.copyTo(gos, 2048)
            }
        }

        Timber.v("## compressFile() : ${file.length()} compressed to ${dstFile.length()} bytes")
        dstFile
    } catch (e: Exception) {
        Timber.e(e, "## compressFile() failed")
        null
    } catch (oom: OutOfMemoryError) {
        Timber.e(oom, "## compressFile() failed")
        null
    }
}
