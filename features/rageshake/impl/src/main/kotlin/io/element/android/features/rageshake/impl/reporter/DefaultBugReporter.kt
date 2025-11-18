/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import android.content.Context
import android.os.Build
import androidx.core.net.toFile
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.rageshake.api.logs.createWriteToFilesConfiguration
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.impl.crash.CrashDataStore
import io.element.android.features.rageshake.impl.screenshot.ScreenshotHolder
import io.element.android.libraries.androidutils.file.compressFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.SdkMetadata
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.sessionIdFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

/**
 * BugReporter creates and sends the bug reports.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultBugReporter(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val screenshotHolder: ScreenshotHolder,
    private val crashDataStore: CrashDataStore,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val okHttpClient: Provider<OkHttpClient>,
    private val userAgentProvider: UserAgentProvider,
    private val sessionStore: SessionStore,
    private val buildMeta: BuildMeta,
    private val bugReporterUrlProvider: BugReporterUrlProvider,
    private val sdkMetadata: SdkMetadata,
    private val matrixClientProvider: MatrixClientProvider,
    private val tracingService: TracingService,
) : BugReporter {
    companion object {
        // filenames
        private const val LOG_CAT_FILENAME = "logcat.log"
        private const val LOG_DIRECTORY_NAME = "logs"
    }

    private val logcatCommandDebug = arrayOf("logcat", "-d", "-v", "threadtime", "*:*")
    private var currentTracingLogLevel: String? = null

    private val baseLogDirectory = File(context.cacheDir, LOG_DIRECTORY_NAME)
    private var currentLogDirectory: File = baseLogDirectory

    init {
        if (buildMeta.isEnterpriseBuild) {
            val logSubfolder = runBlocking {
                sessionStore.getLatestSession()
            }?.userId?.let(::UserId)?.domainName
            setCurrentLogDirectory(logSubfolder)
            sessionStore.sessionIdFlow()
                .map {
                    it?.let(::UserId)?.domainName
                }
                .distinctUntilChanged()
                .onEach { logSubfolder ->
                    setCurrentLogDirectory(logSubfolder)
                    tracingService.updateWriteToFilesConfiguration(createWriteToFilesConfiguration())
                }
                .launchIn(appCoroutineScope)
        }
    }

    override suspend fun sendBugReport(
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withScreenshot: Boolean,
        problemDescription: String,
        canContact: Boolean,
        sendPushRules: Boolean,
        listener: BugReporterListener,
    ) {
        val url = bugReporterUrlProvider.provide().first()
        if (url == null) {
            // It should not happen, but if the URL is null, we cannot proceed
            Timber.e("## sendBugReport() : bug report URL is null")
            error("Bug report URL is null, cannot send bug report")
        }
        // enumerate files to delete
        val bugReportFiles: MutableList<File> = ArrayList()
        var response: Response? = null
        try {
            var serverError: String? = null
            withContext(coroutineDispatchers.io) {
                val crashCallStack = crashDataStore.crashInfo().first()
                val bugDescription = buildString {
                    append(problemDescription)
                    if (crashCallStack.isNotEmpty() && withCrashLogs) {
                        append("\n\n\n\n--------------------------------- crash call stack ---------------------------------\n")
                        append(crashCallStack)
                    }
                }
                val gzippedFiles = mutableListOf<File>()
                if (withDevicesLogs) {
                    val files = getLogFiles().sortedByDescending { it.lastModified() }
                    files.mapNotNullTo(gzippedFiles) { file ->
                        when {
                            file.extension == "gz" -> file
                            else -> compressFile(file)
                        }
                    }
                }
                if (withCrashLogs || withDevicesLogs) {
                    saveLogCat()
                        ?.let { logCatFile ->
                            compressFile(logCatFile).also {
                                logCatFile.safeDelete()
                            }
                        }
                        ?.let { gzippedLogcat ->
                            gzippedFiles.add(0, gzippedLogcat)
                        }
                }
                val sessionData = sessionStore.getLatestSession()
                val numberOfAccounts = sessionStore.numberOfSessions()
                val deviceId = sessionData?.deviceId ?: "undefined"
                val userId = sessionData?.userId?.let { UserId(it) }
                // build the multi part request
                val builder = BugReporterMultipartBody.Builder()
                    .addFormDataPart("text", bugDescription)
                    .addFormDataPart("app", RageshakeConfig.BUG_REPORT_APP_NAME)
                    .addFormDataPart("user_agent", userAgentProvider.provide())
                    .addFormDataPart("user_id", userId?.toString() ?: "undefined")
                    .addFormDataPart("number_of_accounts", numberOfAccounts.toString())
                    .addFormDataPart("can_contact", canContact.toString())
                    .addFormDataPart("device_id", deviceId)
                    .addFormDataPart("device", Build.MODEL.trim())
                    .addFormDataPart("locale", Locale.getDefault().toString())
                    .addFormDataPart("sdk_sha", sdkMetadata.sdkGitSha)
                    .addFormDataPart("local_time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .addFormDataPart("utc_time", LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                    .addFormDataPart("app_id", buildMeta.applicationId)
                    // Nightly versions have a custom version name suffix that we should remove for the bug report
                    .addFormDataPart("Version", buildMeta.versionName.replace("-nightly", ""))
                    .addFormDataPart("label", buildMeta.versionName)
                    .addFormDataPart("label", buildMeta.flavorDescription)
                    .addFormDataPart("branch_name", buildMeta.gitBranchName)
                userId?.let {
                    matrixClientProvider.getOrNull(it)?.let { client ->
                        val curveKey = client.encryptionService.deviceCurve25519()
                        val edKey = client.encryptionService.deviceEd25519()
                        if (curveKey != null && edKey != null) {
                            builder.addFormDataPart("device_keys", "curve25519:$curveKey, ed25519:$edKey")
                        }

                        if (sendPushRules) {
                            client.notificationSettingsService.getRawPushRules().getOrNull()?.let { pushRules ->
                                builder.addFormDataPart(
                                    name = "file",
                                    filename = "push_rules.json",
                                    body = pushRules.toByteArray().toRequestBody(MimeTypes.Json.toMediaTypeOrNull())
                                )
                            }
                        }
                    }
                }
                if (crashCallStack.isNotEmpty() && withCrashLogs) {
                    builder.addFormDataPart("label", "crash")
                }
                currentTracingLogLevel?.let {
                    builder.addFormDataPart("tracing_log_level", it)
                }
                if (buildMeta.isEnterpriseBuild) {
                    builder.addFormDataPart("label", "Enterprise")
                }
                // add the gzipped files, don't cancel the whole upload if only some file failed to upload
                var totalUploadedSize = 0L
                var uploadedSomeLogs = false
                for (file in gzippedFiles) {
                    try {
                        val requestBody = file.asRequestBody(MimeTypes.OctetStream.toMediaTypeOrNull())
                        totalUploadedSize += requestBody.contentLength()
                        // If we are about to upload more than the max request size, stop here
                        if (totalUploadedSize > RageshakeConfig.MAX_LOG_UPLOAD_SIZE) {
                            Timber.e("Could not upload file ${file.name} because it would exceed the max request size")
                            break
                        }
                        builder.addFormDataPart("compressed-log", file.name, requestBody)
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
                    Timber.v("## onWrite() : $percentage%")
                    listener.onProgress(percentage)
                }
                // build the request
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                var errorMessage: String? = null
                // trigger the request
                try {
                    response = okHttpClient()
                        .newCall(request)
                        .execute()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Error executing the request")
                    errorMessage = e.localizedMessage
                }
                val responseCode = response?.code
                // if the upload failed, try to retrieve the reason
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    serverError = if (errorMessage != null) {
                        "Failed with error $errorMessage"
                    } else {
                        val responseBody = response?.body
                        if (responseBody == null) {
                            "Failed with error $responseCode"
                        } else {
                            try {
                                val inputStream = responseBody.byteStream()
                                val serverErrorJson = inputStream.use {
                                    it.readBytes().toString(Charsets.UTF_8)
                                }
                                try {
                                    val responseJSON = JSONObject(serverErrorJson)
                                    responseJSON.getString("error")
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: JSONException) {
                                    Timber.e(e, "Json conversion failed")
                                    "Failed with error $responseCode"
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Timber.e(e, "## sendBugReport() : failed to parse error")
                                "Failed with error $responseCode"
                            }
                        }
                    }
                }
            }
            if (serverError == null) {
                listener.onUploadSucceed()
            } else {
                listener.onUploadFailed(serverError)
            }
        } finally {
            withContext(coroutineDispatchers.io) {
                // delete the generated files when the bug report process has finished
                for (file in bugReportFiles) {
                    file.safeDelete()
                }
                response?.close()
            }
        }
    }

    override fun logDirectory(): File {
        return currentLogDirectory.apply {
            mkdirs()
        }
    }

    private fun setCurrentLogDirectory(subfolderName: String?) {
        currentLogDirectory = if (subfolderName == null) {
            baseLogDirectory
        } else {
            File(baseLogDirectory, subfolderName)
        }
    }

    suspend fun deleteAllFiles(predicate: (File) -> Boolean) {
        withContext(coroutineDispatchers.io) {
            deleteAllFilesRecursive(baseLogDirectory, predicate)
        }
    }

    private fun deleteAllFilesRecursive(
        directory: File,
        predicate: (File) -> Boolean,
    ) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteAllFilesRecursive(file, predicate)
            } else {
                if (predicate(file)) {
                    file.safeDelete()
                }
            }
        }
    }

    override fun setCurrentTracingLogLevel(logLevel: String) {
        currentTracingLogLevel = logLevel
    }

    /**
     * @return the files on the log directory.
     */
    private fun getLogFiles(): List<File> {
        return tryOrNull(
            onException = { Timber.e(it, "## getLogFiles() failed") }
        ) {
            val logDirectory = logDirectory()
            logDirectory.listFiles()
                ?.filter { it.isFile && !it.name.endsWith(LOG_CAT_FILENAME) }
        }.orEmpty()
    }

    // ==============================================================================================================
    // Logcat management
    // ==============================================================================================================

    /**
     * Save the logcat.
     *
     * @return the file if the operation succeeds
     */
    override fun saveLogCat(): File? {
        val file = File(baseLogDirectory, LOG_CAT_FILENAME)
        if (file.exists()) {
            file.safeDelete()
        }
        return try {
            file.writer().use {
                getLogCatContent(it)
            }
            file
        } catch (e: Exception) {
            Timber.e(e, "## saveLogCat() : fail to write logcat")
            null
        }
    }

    /**
     * Retrieves the logs.
     *
     * @param streamWriter the stream writer
     */
    private fun getLogCatContent(streamWriter: OutputStreamWriter) {
        val logcatProcess = tryOrNull {
            Runtime.getRuntime().exec(logcatCommandDebug)
        } ?: return
        try {
            val separator = System.lineSeparator()
            logcatProcess.inputStream
                .reader()
                .buffered(RageshakeConfig.MAX_LOG_UPLOAD_SIZE.toInt())
                .forEachLine { line ->
                    streamWriter.append(line)
                    streamWriter.append(separator)
                }
        } catch (e: IOException) {
            Timber.e(e, "getLogCatContent fails")
        }
    }
}
