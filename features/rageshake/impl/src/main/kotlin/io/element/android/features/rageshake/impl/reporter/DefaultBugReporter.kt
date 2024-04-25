/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.rageshake.impl.reporter

import android.content.Context
import android.os.Build
import android.text.format.DateUtils.DAY_IN_MILLIS
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.crash.CrashDataStore
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.api.screenshot.ScreenshotHolder
import io.element.android.features.rageshake.impl.R
import io.element.android.libraries.androidutils.file.compressFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.SdkMetadata
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider

/**
 * BugReporter creates and sends the bug reports.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultBugReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotHolder: ScreenshotHolder,
    private val crashDataStore: CrashDataStore,
    private val coroutineScope: CoroutineScope,
    private val systemClock: SystemClock,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val okHttpClient: Provider<OkHttpClient>,
    private val userAgentProvider: UserAgentProvider,
    private val sessionStore: SessionStore,
    private val buildMeta: BuildMeta,
    private val bugReporterUrlProvider: BugReporterUrlProvider,
    private val sdkMetadata: SdkMetadata,
) : BugReporter {
    companion object {
        // filenames
        private const val LOG_CAT_FILENAME = "logcat.log"
        private const val LOG_DIRECTORY_NAME = "logs"
        private const val BUFFER_SIZE = 1024 * 1024 * 50
    }

    // the pending bug report call
    private var bugReportCall: Call? = null

    // boolean to cancel the bug report
    private val isCancelled = false
    private val logcatCommandDebug = arrayOf("logcat", "-d", "-v", "threadtime", "*:*")
    private var currentTracingFilter: String? = null

    private val logCatErrFile = File(logDirectory().absolutePath, LOG_CAT_FILENAME)

    override suspend fun sendBugReport(
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withScreenshot: Boolean,
        theBugDescription: String,
        canContact: Boolean,
        listener: BugReporterListener?
    ) {
        // enumerate files to delete
        val bugReportFiles: MutableList<File> = ArrayList()
        try {
            var serverError: String? = null
            withContext(coroutineDispatchers.io) {
                var bugDescription = theBugDescription
                val crashCallStack = crashDataStore.crashInfo().first()

                if (crashCallStack.isNotEmpty() && withCrashLogs) {
                    bugDescription += "\n\n\n\n--------------------------------- crash call stack ---------------------------------\n"
                    bugDescription += crashCallStack
                }

                val gzippedFiles = ArrayList<File>()

                if (withDevicesLogs) {
                    val files = getLogFiles()
                    files.mapNotNullTo(gzippedFiles) { f ->
                        when {
                            isCancelled -> null
                            f.extension == "gz" -> f
                            else -> compressFile(f)
                        }
                    }
                    files.deleteAllExceptMostRecent()
                }

                if (!isCancelled && (withCrashLogs || withDevicesLogs)) {
                    saveLogCat()
                    val gzippedLogcat = compressFile(logCatErrFile)
                    if (null != gzippedLogcat) {
                        if (gzippedFiles.size == 0) {
                            gzippedFiles.add(gzippedLogcat)
                        } else {
                            gzippedFiles.add(0, gzippedLogcat)
                        }
                    }
                }

                val sessionData = sessionStore.getLatestSession()
                val deviceId = sessionData?.deviceId ?: "undefined"
                val userId = sessionData?.userId ?: "undefined"

                if (!isCancelled) {
                    // build the multi part request
                    val builder = BugReporterMultipartBody.Builder()
                        .addFormDataPart("text", bugDescription)
                        .addFormDataPart("app", context.getString(R.string.bug_report_app_name))
                        .addFormDataPart("user_agent", userAgentProvider.provide())
                        .addFormDataPart("user_id", userId)
                        .addFormDataPart("can_contact", canContact.toString())
                        .addFormDataPart("device_id", deviceId)
                        .addFormDataPart("device", Build.MODEL.trim())
                        .addFormDataPart("locale", Locale.getDefault().toString())
                        .addFormDataPart("sdk_sha", sdkMetadata.sdkGitSha)
                        .addFormDataPart("local_time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .addFormDataPart("utc_time", LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                    currentTracingFilter?.let {
                        builder.addFormDataPart("tracing_filter", it)
                    }

                    // add the gzipped files, don't cancel the whole upload if only some file failed to upload
                    var uploadedSomeLogs = false
                    for (file in gzippedFiles) {
                        try {
                            builder.addFormDataPart("compressed-log", file.name, file.asRequestBody(MimeTypes.OctetStream.toMediaTypeOrNull()))
                            uploadedSomeLogs = true
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Timber.e(e, "## sendBugReport() : fail to attach file ${file.name}")
                        }
                    }

                    bugReportFiles.addAll(gzippedFiles)

                    if (gzippedFiles.isNotEmpty() && !uploadedSomeLogs) {
                        serverError = "Couldn't upload any logs, please retry."
                        return@withContext
                    }

                    if (withScreenshot) {
                        screenshotHolder.getFileUri()
                            ?.toUri()
                            ?.toFile()
                            ?.let { screenshotFile ->
                                try {
                                    builder.addFormDataPart(
                                        "file",
                                        screenshotFile.name,
                                        screenshotFile.asRequestBody(MimeTypes.OctetStream.toMediaTypeOrNull())
                                    )
                                } catch (e: Exception) {
                                    Timber.e(e, "## sendBugReport() : fail to write screenshot")
                                }
                            }
                    }

                    // add some github labels
                    builder.addFormDataPart("label", buildMeta.versionName)
                    builder.addFormDataPart("label", buildMeta.flavorDescription)
                    builder.addFormDataPart("branch_name", buildMeta.gitBranchName)

                    if (crashCallStack.isNotEmpty() && withCrashLogs) {
                        builder.addFormDataPart("label", "crash")
                    }

                    val requestBody = builder.build()

                    // add a progress listener
                    requestBody.setWriteListener { totalWritten, contentLength ->
                        val percentage = if (-1L != contentLength) {
                            if (totalWritten > contentLength) {
                                100
                            } else {
                                (totalWritten * 100 / contentLength).toInt()
                            }
                        } else {
                            0
                        }

                        if (isCancelled && null != bugReportCall) {
                            bugReportCall!!.cancel()
                        }

                        Timber.v("## onWrite() : $percentage%")
                        try {
                            listener?.onProgress(percentage)
                        } catch (e: Exception) {
                            Timber.e(e, "## onProgress() : failed")
                        }
                    }

                    // build the request
                    val request = Request.Builder()
                        .url(bugReporterUrlProvider.provide())
                        .post(requestBody)
                        .build()

                    var responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR
                    var response: Response? = null
                    var errorMessage: String? = null

                    // trigger the request
                    try {
                        bugReportCall = okHttpClient.get().newCall(request)
                        response = bugReportCall!!.execute()
                        responseCode = response.code
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Timber.e(e, "response")
                        errorMessage = e.localizedMessage
                    }

                    // if the upload failed, try to retrieve the reason
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        if (null != errorMessage) {
                            serverError = "Failed with error $errorMessage"
                        } else if (response?.body == null) {
                            serverError = "Failed with error $responseCode"
                        } else {
                            try {
                                val inputStream = response.body!!.byteStream()
                                serverError = inputStream.use {
                                    buildString {
                                        var ch = it.read()
                                        while (ch != -1) {
                                            append(ch.toChar())
                                            ch = it.read()
                                        }
                                    }
                                }
                                // check if the error message
                                serverError?.let {
                                    try {
                                        val responseJSON = JSONObject(it)
                                        serverError = responseJSON.getString("error")
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: JSONException) {
                                        Timber.e(e, "doInBackground ; Json conversion failed")
                                    }
                                }
                                // should never happen
                                if (null == serverError) {
                                    serverError = "Failed with error $responseCode"
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Timber.e(e, "## sendBugReport() : failed to parse error")
                            }
                        }
                    }
                }
            }
            withContext(coroutineDispatchers.main) {
                bugReportCall = null
                if (null != listener) {
                    try {
                        if (isCancelled) {
                            listener.onUploadCancelled()
                        } else if (null == serverError) {
                            listener.onUploadSucceed()
                        } else {
                            listener.onUploadFailed(serverError)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Timber.e(e, "## onPostExecute() : failed")
                    }
                }
            }
        } finally {
            // delete the generated files when the bug report process has finished
            for (file in bugReportFiles) {
                file.safeDelete()
            }
        }
    }

    override fun logDirectory(): File {
        return File(context.cacheDir, LOG_DIRECTORY_NAME).apply {
            mkdirs()
        }
    }

    override fun cleanLogDirectoryIfNeeded() {
        coroutineScope.launch(coroutineDispatchers.io) {
            // delete the log files older than 1 day, except the most recent one
            deleteOldLogFiles(systemClock.epochMillis() - DAY_IN_MILLIS)
        }
    }

    suspend fun deleteAllFiles() {
        withContext(coroutineDispatchers.io) {
            getLogFiles().forEach { it.safeDelete() }
        }
    }

    override fun setCurrentTracingFilter(tracingFilter: String) {
        currentTracingFilter = tracingFilter
    }

    /**
     * @return the files on the log directory.
     */
    private fun getLogFiles(): List<File> {
        return tryOrNull(
            onError = { Timber.e(it, "## getLogFiles() failed") }
        ) {
            val logDirectory = logDirectory()
            logDirectory.listFiles()?.toList()
        }.orEmpty()
    }

    /**
     * Delete the log files older than the given time except the most recent one.
     * @param time the time in ms
     */
    private fun deleteOldLogFiles(time: Long) {
        val logFiles = getLogFiles()
        val oldLogFiles = logFiles.filter { it.lastModified() < time }
        oldLogFiles.deleteAllExceptMostRecent()
    }

    /**
     * Delete all the log files except the most recent one.
     */
    private fun List<File>.deleteAllExceptMostRecent() {
        if (size > 1) {
            val mostRecentFile = maxByOrNull { it.lastModified() }
            forEach { file ->
                if (file != mostRecentFile) {
                    file.safeDelete()
                }
            }
        }
    }

    // ==============================================================================================================
    // Logcat management
    // ==============================================================================================================

    /**
     * Save the logcat.
     *
     * @return the file if the operation succeeds
     */
    override fun saveLogCat() {
        if (logCatErrFile.exists()) {
            logCatErrFile.safeDelete()
        }
        try {
            logCatErrFile.writer().use {
                getLogCatError(it)
            }
        } catch (error: OutOfMemoryError) {
            Timber.e(error, "## saveLogCat() : fail to write logcat OOM")
        } catch (e: Exception) {
            Timber.e(e, "## saveLogCat() : fail to write logcat")
        }
    }

    /**
     * Retrieves the logs.
     *
     * @param streamWriter the stream writer
     */
    private fun getLogCatError(streamWriter: OutputStreamWriter) {
        val logcatProc: Process

        try {
            logcatProc = Runtime.getRuntime().exec(logcatCommandDebug)
        } catch (e1: IOException) {
            return
        }

        try {
            val separator = System.getProperty("line.separator")
            logcatProc.inputStream
                .reader()
                .buffered(BUFFER_SIZE)
                .forEachLine { line ->
                    streamWriter.append(line)
                    streamWriter.append(separator)
                }
        } catch (e: IOException) {
            Timber.e(e, "getLog fails")
        }
    }
}
