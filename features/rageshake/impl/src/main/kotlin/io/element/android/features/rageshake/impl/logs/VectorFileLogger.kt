/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.rageshake.impl.logs

import android.content.Context
import android.util.Log
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.data.tryOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Will be planted in Timber.
 */
class VectorFileLogger(
    private val context: Context,
    // private val vectorPreferences: VectorPreferences
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Timber.Tree() {

    companion object {
        fun getFromTimber(): VectorFileLogger? {
            return Timber.forest().filterIsInstance<VectorFileLogger>().firstOrNull()
        }

        private const val SIZE_20MB = 20 * 1024 * 1024
        // private const val SIZE_50MB = 50 * 1024 * 1024
    }

    /*
    private val maxLogSizeByte = if (vectorPreferences.labAllowedExtendedLogging()) SIZE_50MB else SIZE_20MB
    private val logRotationCount = if (vectorPreferences.labAllowedExtendedLogging()) 15 else 7
     */
    private val maxLogSizeByte = SIZE_20MB
    private val logRotationCount = 7

    private val logger = Logger.getLogger(context.packageName).apply {
        tryOrNull {
            useParentHandlers = false
            level = Level.ALL
        }
    }

    private val fileHandler: FileHandler?
    private val cacheDirectory get() = File(context.cacheDir, "logs").apply {
        if (!exists()) mkdirs()
    }
    private var fileNamePrefix = "logs"

    private val prioPrefixes = mapOf(
        Log.VERBOSE to "V/ ",
        Log.DEBUG to "D/ ",
        Log.INFO to "I/ ",
        Log.WARN to "W/ ",
        Log.ERROR to "E/ ",
        Log.ASSERT to "WTF/ "
    )

    init {
        for (i in 0..15) {
            val file = File(cacheDirectory, "elementLogs.$i.txt")
            file.safeDelete()
        }

        fileHandler = tryOrNull(
            onError = { Timber.e(it, "Failed to initialize FileLogger") }
        ) {
            FileHandler(
                cacheDirectory.absolutePath + "/" + fileNamePrefix + ".%g.txt",
                maxLogSizeByte,
                logRotationCount
            )
                .also { it.formatter = LogFormatter() }
                .also { logger.addHandler(it) }
        }
    }

    fun reset() {
        // Delete all files
        getLogFiles().map {
            it.safeDelete()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        fileHandler ?: return
        GlobalScope.launch(dispatcher) {
            if (skipLog(priority)) return@launch
            if (t != null) {
                logToFile(t)
            }
            logToFile(prioPrefixes[priority] ?: "$priority ", tag ?: "Tag", message)
        }
    }

    private fun skipLog(priority: Int): Boolean {
        /*
        return if (vectorPreferences.labAllowedExtendedLogging()) {
            false
        } else {
            // Exclude verbose logs
            priority < Log.DEBUG
        }
         */
        // Exclude verbose logs
        return priority < Log.DEBUG
    }

    /**
     * Adds our own log files to the provided list of files.
     *
     * @return The list of files with logs.
     */
    private fun getLogFiles(): List<File> {
        return tryOrNull(
            onError = { Timber.e(it, "## getLogFiles() failed") }
        ) {
            fileHandler
                ?.flush()
                ?.let { 0 until logRotationCount }
                ?.mapNotNull { index ->
                    File(cacheDirectory, "$fileNamePrefix.$index.txt")
                        .takeIf { it.exists() }
                }
        }
            .orEmpty()
    }

    /**
     * Log an Throwable.
     *
     * @param throwable the throwable to log
     */
    private fun logToFile(throwable: Throwable?) {
        throwable ?: return

        val errors = StringWriter()
        throwable.printStackTrace(PrintWriter(errors))

        logger.info(errors.toString())
    }

    private fun logToFile(level: String, tag: String, content: String) {
        val b = StringBuilder()
        b.append(Thread.currentThread().id)
        b.append(" ")
        b.append(level)
        b.append("/")
        b.append(tag)
        b.append(": ")
        b.append(content)
        logger.info(b.toString())
    }
}
