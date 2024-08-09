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

package io.element.android.features.rageshake.impl.crash

import android.content.Context
import android.os.Build
import io.element.android.libraries.core.data.tryOrNull
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

class VectorUncaughtExceptionHandler(
    context: Context
) : Thread.UncaughtExceptionHandler {
    private val crashDataStore = PreferencesCrashDataStore(context)
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * Activate this handler.
     */
    fun activate() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * An uncaught exception has been triggered.
     *
     * @param thread the thread
     * @param throwable the throwable
     */
    @Suppress("PrintStackTrace")
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Timber.v("Uncaught exception: $throwable")
        val bugDescription = buildString {
            val appName = "TchapX"
            // append(appName + " Build : " + versionCodeProvider.getVersionCode() + "\n")
            append("$appName Version : 1.0") // ${versionProvider.getVersion(longFormat = true)}\n")
            // append("SDK Version : ${Matrix.getSdkVersion()}\n")
            append("Phone : " + Build.MODEL.trim() + " (" + Build.VERSION.INCREMENTAL + " " + Build.VERSION.RELEASE + " " + Build.VERSION.CODENAME + ")\n")
            append("Memory statuses \n")
            var freeSize = 0L
            var totalSize = 0L
            var usedSize = -1L
            tryOrNull {
                val info = Runtime.getRuntime()
                freeSize = info.freeMemory()
                totalSize = info.totalMemory()
                usedSize = totalSize - freeSize
            }
            append("usedSize   " + usedSize / 1_048_576L + " MB\n")
            append("freeSize   " + freeSize / 1_048_576L + " MB\n")
            append("totalSize   " + totalSize / 1_048_576L + " MB\n")
            append("Thread: ")
            append(thread.name)
            append(", Exception: ")
            val sw = StringWriter()
            val pw = PrintWriter(sw, true)
            throwable.printStackTrace(pw)
            append(sw.buffer.toString())
        }
        Timber.e("FATAL EXCEPTION $bugDescription")
        crashDataStore.setCrashData(bugDescription)
        // Show the classical system popup
        previousHandler?.uncaughtException(thread, throwable)
    }
}
