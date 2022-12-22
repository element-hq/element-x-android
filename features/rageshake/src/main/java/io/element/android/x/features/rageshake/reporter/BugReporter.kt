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

package io.element.android.x.features.rageshake.reporter

import android.content.Context
import android.os.Build
import io.element.android.x.core.extensions.toOnOff
import io.element.android.x.core.file.compressFile
import io.element.android.x.core.mimetype.MimeTypes
import io.element.android.x.di.ApplicationContext
import io.element.android.x.features.rageshake.R
import io.element.android.x.features.rageshake.crash.CrashDataStore
import io.element.android.x.features.rageshake.logs.VectorFileLogger
import io.element.android.x.features.rageshake.screenshot.ScreenshotHolder
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

/**
 * BugReporter creates and sends the bug reports.
 */
class BugReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotHolder: ScreenshotHolder,
    private val crashDataStore: CrashDataStore,
    /*
    private val activeSessionHolder: ActiveSessionHolder,
    private val versionProvider: VersionProvider,
    private val vectorPreferences: VectorPreferences,
    private val vectorFileLogger: VectorFileLogger,
    private val systemLocaleProvider: SystemLocaleProvider,
    private val matrix: Matrix,
    private val buildMeta: BuildMeta,
    private val processInfo: ProcessInfo,
    private val sdkIntProvider: BuildVersionSdkIntProvider,
    private val vectorLocale: VectorLocaleProvider,
     */
) {
    var inMultiWindowMode = false

    companion object {
        // filenames
        private const val LOG_CAT_ERROR_FILENAME = "logcatError.log"
        private const val LOG_CAT_FILENAME = "logcat.log"
        private const val KEY_REQUESTS_FILENAME = "keyRequests.log"

        private const val BUFFER_SIZE = 1024 * 1024 * 50
    }

    // the http client
    private val mOkHttpClient = OkHttpClient()

    // the pending bug report call
    private var mBugReportCall: Call? = null

    // boolean to cancel the bug report
    private val mIsCancelled = false

    /*
    val adapter = MatrixJsonParser.getMoshi()
            .adapter<JsonDict>(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
     */

    private val LOGCAT_CMD_ERROR = arrayOf(
        "logcat", // /< Run 'logcat' command
        "-d", // /< Dump the log rather than continue outputting it
        "-v", // formatting
        "threadtime", // include timestamps
        "AndroidRuntime:E " + // /< Pick all AndroidRuntime errors (such as uncaught exceptions)"communicatorjni:V " + ///< All communicatorjni logging
            "libcommunicator:V " + // /< All libcommunicator logging
            "DEBUG:V " + // /< All DEBUG logging - which includes native land crashes (seg faults, etc)
            "*:S" // /< Everything else silent, so don't pick it..
    )

    private val LOGCAT_CMD_DEBUG = arrayOf("logcat", "-d", "-v", "threadtime", "*:*")

    /**
     * Bug report upload listener.
     */
    interface IMXBugReportListener {
        /**
         * The bug report has been cancelled.
         */
        fun onUploadCancelled()

        /**
         * The bug report upload failed.
         *
         * @param reason the failure reason
         */
        fun onUploadFailed(reason: String?)

        /**
         * The upload progress (in percent).
         *
         * @param progress the upload progress
         */
        fun onProgress(progress: Int)

        /**
         * The bug report upload succeeded.
         */
        fun onUploadSucceed(reportUrl: String?)
    }

    /**
     * Send a bug report.
     *
     * @param coroutineScope The coroutine scope
     * @param reportType The report type (bug, suggestion, feedback)
     * @param withDevicesLogs true to include the device log
     * @param withCrashLogs true to include the crash logs
     * @param withKeyRequestHistory true to include the crash logs
     * @param withScreenshot true to include the screenshot
     * @param theBugDescription the bug description
     * @param serverVersion version of the server
     * @param canContact true if the user opt in to be contacted directly
     * @param customFields fields which will be sent with the report
     * @param listener the listener
     */
    fun sendBugReport(
        coroutineScope: CoroutineScope,
        reportType: ReportType,
        withDevicesLogs: Boolean,
        withCrashLogs: Boolean,
        withKeyRequestHistory: Boolean,
        withScreenshot: Boolean,
        theBugDescription: String,
        serverVersion: String,
        canContact: Boolean = false,
        customFields: Map<String, String>? = null,
        listener: IMXBugReportListener?
    ) {
        // enumerate files to delete
        val mBugReportFiles: MutableList<File> = ArrayList()

        coroutineScope.launch {
            var serverError: String? = null
            var reportURL: String? = null
            withContext(Dispatchers.IO) {
                var bugDescription = theBugDescription
                val crashCallStack = crashDataStore.crashInfo().first()

                if (crashCallStack.isNotEmpty() && withCrashLogs) {
                    bugDescription += "\n\n\n\n--------------------------------- crash call stack ---------------------------------\n"
                    bugDescription += crashCallStack
                }

                val gzippedFiles = ArrayList<File>()

                val vectorFileLogger = VectorFileLogger.getFromTimber()
                if (withDevicesLogs) {
                    val files = vectorFileLogger.getLogFiles()
                    files.mapNotNullTo(gzippedFiles) { f ->
                        if (!mIsCancelled) {
                            compressFile(f)
                        } else {
                            null
                        }
                    }
                }

                if (!mIsCancelled && (withCrashLogs || withDevicesLogs)) {
                    val gzippedLogcat = saveLogCat(false)

                    if (null != gzippedLogcat) {
                        if (gzippedFiles.size == 0) {
                            gzippedFiles.add(gzippedLogcat)
                        } else {
                            gzippedFiles.add(0, gzippedLogcat)
                        }
                    }
                }

                /*
                activeSessionHolder.getSafeActiveSession()
                        ?.takeIf { !mIsCancelled && withKeyRequestHistory }
                        ?.cryptoService()
                        ?.getGossipingEvents()
                        ?.let { GossipingEventsSerializer().serialize(it) }
                        ?.toByteArray()
                        ?.let { rawByteArray ->
                            File(context.cacheDir.absolutePath, KEY_REQUESTS_FILENAME)
                                    .also {
                                        it.outputStream()
                                                .use { os -> os.write(rawByteArray) }
                                    }
                        }
                        ?.let { compressFile(it) }
                        ?.let { gzippedFiles.add(it) }
                 */

                var deviceId = "undefined"
                var userId = "undefined"
                var olmVersion = "undefined"

                /*
                activeSessionHolder.getSafeActiveSession()?.let { session ->
                    userId = session.myUserId
                    deviceId = session.sessionParams.deviceId ?: "undefined"
                    olmVersion = session.cryptoService().getCryptoVersion(context, true)
                }
                 */

                if (!mIsCancelled) {
                    val text = when (reportType) {
                        ReportType.BUG_REPORT -> "[ElementX] $bugDescription"
                        ReportType.SUGGESTION -> "[ElementX] [Suggestion] $bugDescription"
                        ReportType.SPACE_BETA_FEEDBACK -> "[ElementX] [spaces-feedback] $bugDescription"
                        ReportType.THREADS_BETA_FEEDBACK -> "[ElementX] [threads-feedback] $bugDescription"
                        ReportType.AUTO_UISI_SENDER,
                        ReportType.AUTO_UISI -> bugDescription
                    }

                    // build the multi part request
                    val builder = BugReporterMultipartBody.Builder()
                        .addFormDataPart("text", text)
                        .addFormDataPart("app", rageShakeAppNameForReport(reportType))
                        // .addFormDataPart("user_agent", matrix.getUserAgent())
                        .addFormDataPart("user_id", userId)
                        .addFormDataPart("can_contact", canContact.toString())
                        .addFormDataPart("device_id", deviceId)
                        // .addFormDataPart("version", versionProvider.getVersion(longFormat = true))
                        // .addFormDataPart("branch_name", buildMeta.gitBranchName)
                        // .addFormDataPart("matrix_sdk_version", Matrix.getSdkVersion())
                        .addFormDataPart("olm_version", olmVersion)
                        .addFormDataPart("device", Build.MODEL.trim())
                        // .addFormDataPart("verbose_log", vectorPreferences.labAllowedExtendedLogging().toOnOff())
                        .addFormDataPart("multi_window", inMultiWindowMode.toOnOff())
                        // .addFormDataPart(
                        //        "os", Build.VERSION.RELEASE + " (API " + sdkIntProvider.get() + ") " +
                        //        Build.VERSION.INCREMENTAL + "-" + Build.VERSION.CODENAME
                        // )
                        .addFormDataPart("locale", Locale.getDefault().toString())
                        // .addFormDataPart("app_language", vectorLocale.applicationLocale.toString())
                        // .addFormDataPart("default_app_language", systemLocaleProvider.getSystemLocale().toString())
                        // .addFormDataPart("theme", ThemeUtils.getApplicationTheme(context))
                        .addFormDataPart("server_version", serverVersion)
                        .apply {
                            customFields?.forEach { (name, value) ->
                                addFormDataPart(name, value)
                            }
                        }

                    // add the gzipped files
                    for (file in gzippedFiles) {
                        builder.addFormDataPart("compressed-log", file.name, file.asRequestBody(MimeTypes.OctetStream.toMediaTypeOrNull()))
                    }

                    mBugReportFiles.addAll(gzippedFiles)

                    if (withScreenshot) {
                        screenshotHolder.getFile()?.let { screenshotFile ->
                            try {
                                builder.addFormDataPart(
                                    "file",
                                    screenshotFile.name, screenshotFile.asRequestBody(MimeTypes.OctetStream.toMediaTypeOrNull())
                                )
                            } catch (e: Exception) {
                                Timber.e(e, "## sendBugReport() : fail to write screenshot")
                            }
                        }
                    }

                    // add some github labels
                    // builder.addFormDataPart("label", buildMeta.versionName)
                    // builder.addFormDataPart("label", buildMeta.flavorDescription)
                    // builder.addFormDataPart("label", buildMeta.gitBranchName)

                    // Special for ElementX
                    builder.addFormDataPart("label", "[ElementX]")

                    // Possible values for BuildConfig.BUILD_TYPE: "debug", "nightly", "release".
                    // builder.addFormDataPart("label", BuildConfig.BUILD_TYPE)

                    when (reportType) {
                        ReportType.BUG_REPORT -> {
                            /* nop */
                        }
                        ReportType.SUGGESTION -> builder.addFormDataPart("label", "[Suggestion]")
                        ReportType.SPACE_BETA_FEEDBACK -> builder.addFormDataPart("label", "spaces-feedback")
                        ReportType.THREADS_BETA_FEEDBACK -> builder.addFormDataPart("label", "threads-feedback")
                        ReportType.AUTO_UISI -> {
                            builder.addFormDataPart("label", "Z-UISI")
                            builder.addFormDataPart("label", "android")
                            builder.addFormDataPart("label", "uisi-recipient")
                        }
                        ReportType.AUTO_UISI_SENDER -> {
                            builder.addFormDataPart("label", "Z-UISI")
                            builder.addFormDataPart("label", "android")
                            builder.addFormDataPart("label", "uisi-sender")
                        }
                    }

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

                        if (mIsCancelled && null != mBugReportCall) {
                            mBugReportCall!!.cancel()
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
                        .url(context.getString(R.string.bug_report_url))
                        .post(requestBody)
                        .build()

                    var responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR
                    var response: Response? = null
                    var errorMessage: String? = null

                    // trigger the request
                    try {
                        mBugReportCall = mOkHttpClient.newCall(request)
                        response = mBugReportCall!!.execute()
                        responseCode = response.code
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
                                    } catch (e: JSONException) {
                                        Timber.e(e, "doInBackground ; Json conversion failed")
                                    }
                                }

                                // should never happen
                                if (null == serverError) {
                                    serverError = "Failed with error $responseCode"
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "## sendBugReport() : failed to parse error")
                            }
                        }
                    } else {
                        /*
                        reportURL = response?.body?.string()?.let { stringBody ->
                            adapter.fromJson(stringBody)?.get("report_url")?.toString()
                        }
                         */
                    }
                }
            }

            withContext(Dispatchers.Main) {
                mBugReportCall = null

                // delete when the bug report has been successfully sent
                for (file in mBugReportFiles) {
                    file.delete()
                }

                if (null != listener) {
                    try {
                        if (mIsCancelled) {
                            listener.onUploadCancelled()
                        } else if (null == serverError) {
                            listener.onUploadSucceed(reportURL)
                        } else {
                            listener.onUploadFailed(serverError)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "## onPostExecute() : failed")
                    }
                }
            }
        }
    }

    /**
     * Send a bug report either with email or with Vector.
     */
    /* TODO Remove
    fun openBugReportScreen(activity: FragmentActivity, reportType: ReportType = ReportType.BUG_REPORT) {
        screenshot = takeScreenshot(activity)
        logDbInfo()
        logProcessInfo()
        logOtherInfo()
        activity.startActivity(BugReportActivity.intent(activity, reportType))
    }
     */

    // private fun logOtherInfo() {
    //    Timber.i("SyncThread state: " + activeSessionHolder.getSafeActiveSession()?.syncService()?.getSyncState())
    // }

    // private fun logDbInfo() {
    //    val dbInfo = matrix.debugService().getDbUsageInfo()
    //    Timber.i(dbInfo)
    // }

    // private fun logProcessInfo() {
    //    val pInfo = processInfo.getInfo()
    //    Timber.i(pInfo)
    // }

    private fun rageShakeAppNameForReport(reportType: ReportType): String {
        // As per https://github.com/matrix-org/rageshake
        // app: Identifier for the application (eg 'riot-web').
        // Should correspond to a mapping configured in the configuration file for github issue reporting to work.
        // (see R.string.bug_report_url for configured RS server)
        return context.getString(
            when (reportType) {
                ReportType.AUTO_UISI_SENDER,
                ReportType.AUTO_UISI -> R.string.bug_report_auto_uisi_app_name
                else -> R.string.bug_report_app_name
            }
        )
    }

    // ==============================================================================================================
    // Logcat management
    // ==============================================================================================================

    /**
     * Save the logcat.
     *
     * @param isErrorLogcat true to save the error logcat
     * @return the file if the operation succeeds
     */
    private fun saveLogCat(isErrorLogcat: Boolean): File? {
        val logCatErrFile = File(context.cacheDir.absolutePath, if (isErrorLogcat) LOG_CAT_ERROR_FILENAME else LOG_CAT_FILENAME)

        if (logCatErrFile.exists()) {
            logCatErrFile.delete()
        }

        try {
            logCatErrFile.writer().use {
                getLogCatError(it, isErrorLogcat)
            }

            return compressFile(logCatErrFile)
        } catch (error: OutOfMemoryError) {
            Timber.e(error, "## saveLogCat() : fail to write logcat$error")
        } catch (e: Exception) {
            Timber.e(e, "## saveLogCat() : fail to write logcat$e")
        }

        return null
    }

    /**
     * Retrieves the logs.
     *
     * @param streamWriter the stream writer
     * @param isErrorLogCat true to save the error logs
     */
    private fun getLogCatError(streamWriter: OutputStreamWriter, isErrorLogCat: Boolean) {
        val logcatProc: Process

        try {
            logcatProc = Runtime.getRuntime().exec(if (isErrorLogCat) LOGCAT_CMD_ERROR else LOGCAT_CMD_DEBUG)
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
