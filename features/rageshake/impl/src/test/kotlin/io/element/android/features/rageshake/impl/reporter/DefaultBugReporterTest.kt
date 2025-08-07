/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.impl.crash.CrashDataStore
import io.element.android.features.rageshake.impl.crash.FakeCrashDataStore
import io.element.android.features.rageshake.impl.screenshot.FakeScreenshotHolder
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.tracing.TracingService
import io.element.android.libraries.matrix.api.tracing.WriteToFilesConfiguration
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.tracing.FakeTracingService
import io.element.android.libraries.network.useragent.DefaultUserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartReader
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.buffer
import okio.source
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultBugReporterTest {
    @Test
    fun `test sendBugReport success`() = runTest {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
        )
        server.start()
        val sut = createDefaultBugReporter(server = server)
        var onUploadCancelledCalled = false
        var onUploadFailedCalled = false
        val progressValues = mutableListOf<Int>()
        var onUploadSucceedCalled = false
        sut.sendBugReport(
            withDevicesLogs = true,
            withCrashLogs = true,
            withScreenshot = true,
            problemDescription = "a bug occurred",
            canContact = true,
            listener = object : BugReporterListener {
                override fun onUploadCancelled() {
                    onUploadCancelledCalled = true
                }

                override fun onUploadFailed(reason: String?) {
                    onUploadFailedCalled = true
                }

                override fun onProgress(progress: Int) {
                    progressValues.add(progress)
                }

                override fun onUploadSucceed() {
                    onUploadSucceedCalled = true
                }
            },
        )
        val request = server.takeRequest()
        assertThat(request.path).isEqualTo("/")
        assertThat(request.method).isEqualTo("POST")
        server.shutdown()
        assertThat(onUploadCancelledCalled).isFalse()
        assertThat(onUploadFailedCalled).isFalse()
        assertThat(progressValues.size).isEqualTo(EXPECTED_NUMBER_OF_PROGRESS_VALUE)
        assertThat(onUploadSucceedCalled).isTrue()
    }

    @Test
    fun `test sendBugReport form data`() = runTest {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
        )
        server.start()

        val mockSessionStore = InMemorySessionStore().apply {
            storeData(aSessionData(sessionId = "@foo:example.com", deviceId = "ABCDEFGH"))
        }

        val fakeEncryptionService = FakeEncryptionService()
        val matrixClient = FakeMatrixClient(encryptionService = fakeEncryptionService)

        fakeEncryptionService.givenDeviceKeys("CURVECURVECURVE", "EDKEYEDKEYEDKY")
        val sut = createDefaultBugReporter(
            server = server,
            crashDataStore = FakeCrashDataStore(),
            sessionStore = mockSessionStore,
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        )

        val progressValues = mutableListOf<Int>()
        sut.sendBugReport(
            withDevicesLogs = true,
            withCrashLogs = true,
            withScreenshot = true,
            problemDescription = "a bug occurred",
            canContact = true,
            listener = object : BugReporterListener {
                override fun onUploadCancelled() {}

                override fun onUploadFailed(reason: String?) {}

                override fun onProgress(progress: Int) {
                    progressValues.add(progress)
                }

                override fun onUploadSucceed() {}
            },
        )
        val request = server.takeRequest()

        val foundValues = collectValuesFromFormData(request)

        assertThat(foundValues["app"]).isEqualTo(RageshakeConfig.BUG_REPORT_APP_NAME)
        assertThat(foundValues["can_contact"]).isEqualTo("true")
        assertThat(foundValues["device_id"]).isEqualTo("ABCDEFGH")
        assertThat(foundValues["sdk_sha"]).isEqualTo("123456789")
        assertThat(foundValues["user_id"]).isEqualTo("@foo:example.com")
        assertThat(foundValues["text"]).isEqualTo("a bug occurred")
        assertThat(foundValues["device_keys"]).isEqualTo("curve25519:CURVECURVECURVE, ed25519:EDKEYEDKEYEDKY")

        // device_key now added given they are not null
        assertThat(progressValues.size).isEqualTo(EXPECTED_NUMBER_OF_PROGRESS_VALUE + 1)

        server.shutdown()
    }

    @Test
    fun `test sendBugReport should not report device_keys if not known`() = runTest {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
        )
        server.start()

        val mockSessionStore = InMemorySessionStore().apply {
            storeData(aSessionData("@foo:example.com", "ABCDEFGH"))
        }

        val fakeEncryptionService = FakeEncryptionService()
        val matrixClient = FakeMatrixClient(encryptionService = fakeEncryptionService)

        fakeEncryptionService.givenDeviceKeys(null, null)
        val sut = createDefaultBugReporter(
            server = server,
            sessionStore = mockSessionStore,
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        )

        sut.sendBugReport(
            withDevicesLogs = true,
            withCrashLogs = true,
            withScreenshot = true,
            problemDescription = "a bug occurred",
            canContact = true,
            listener = NoopBugReporterListener(),
        )
        val request = server.takeRequest()

        val foundValues = collectValuesFromFormData(request)
        assertThat(foundValues["device_keys"]).isNull()
        server.shutdown()
    }

    @Test
    fun `test sendBugReport no client provider no session data`() = runTest {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
        )
        server.start()

        val fakeEncryptionService = FakeEncryptionService()

        fakeEncryptionService.givenDeviceKeys(null, null)
        val sut = createDefaultBugReporter(
            server = server,
            crashDataStore = FakeCrashDataStore("I did crash", true),
            sessionStore = InMemorySessionStore(),
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.failure(Exception("Mock no client")) })
        )

        sut.sendBugReport(
            withDevicesLogs = true,
            withCrashLogs = true,
            withScreenshot = true,
            problemDescription = "a bug occurred",
            canContact = true,
            listener = NoopBugReporterListener(),
        )
        val request = server.takeRequest()

        val foundValues = collectValuesFromFormData(request)
        println("## FOUND VALUES $foundValues")
        assertThat(foundValues["device_keys"]).isNull()
        assertThat(foundValues["device_id"]).isEqualTo("undefined")
        assertThat(foundValues["user_id"]).isEqualTo("undefined")
        assertThat(foundValues["label"]).isEqualTo("crash")
    }

    private fun collectValuesFromFormData(request: RecordedRequest): HashMap<String, String> {
        val boundary = request.headers["Content-Type"]!!.split("=").last()
        val foundValues = HashMap<String, String>()
        request.body.inputStream().source().buffer().use {
            val multipartReader = MultipartReader(it, boundary)
            // Just use simple parsing to detect basic properties
            val regex = "form-data; name=\"(\\w*)\".*".toRegex()
            multipartReader.use {
                var part = multipartReader.nextPart()
                while (part != null) {
                    part.headers["Content-Disposition"]?.let { contentDisposition ->
                        regex.find(contentDisposition)?.groupValues?.get(1)?.let { name ->
                            foundValues.put(name, part!!.body.readUtf8())
                        }
                    }
                    part = multipartReader.nextPart()
                }
            }
        }
        return foundValues
    }

    @Test
    fun `test sendBugReport error`() = runTest {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error": "An error body"}""")
        )
        server.start()
        val sut = createDefaultBugReporter(server = server)
        var onUploadCancelledCalled = false
        var onUploadFailedCalled = false
        var onUploadFailedReason: String? = null
        val progressValues = mutableListOf<Int>()
        var onUploadSucceedCalled = false
        sut.sendBugReport(
            withDevicesLogs = true,
            withCrashLogs = true,
            withScreenshot = true,
            problemDescription = "a bug occurred",
            canContact = true,
            listener = object : BugReporterListener {
                override fun onUploadCancelled() {
                    onUploadCancelledCalled = true
                }

                override fun onUploadFailed(reason: String?) {
                    onUploadFailedCalled = true
                    onUploadFailedReason = reason
                }

                override fun onProgress(progress: Int) {
                    progressValues.add(progress)
                }

                override fun onUploadSucceed() {
                    onUploadSucceedCalled = true
                }
            },
        )
        val request = server.takeRequest()
        assertThat(request.path).isEqualTo("/")
        assertThat(request.method).isEqualTo("POST")
        server.shutdown()
        assertThat(onUploadCancelledCalled).isFalse()
        assertThat(onUploadFailedCalled).isTrue()
        assertThat(onUploadFailedReason).isEqualTo("An error body")
        assertThat(progressValues.size).isEqualTo(EXPECTED_NUMBER_OF_PROGRESS_VALUE)
        assertThat(onUploadSucceedCalled).isFalse()
    }

    @Test
    fun `the log directory is initialized using the last session store data`() = runTest {
        val sut = createDefaultBugReporter(
            buildMeta = aBuildMeta(isEnterpriseBuild = true),
            sessionStore = InMemorySessionStore().apply {
                storeData(aSessionData(sessionId = "@alice:domain.com"))
            }
        )
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs/domain.com")
    }

    @Test
    fun `foss build - the log directory is initialized to the root log directory`() = runTest {
        val sut = createDefaultBugReporter(
            sessionStore = InMemorySessionStore().apply {
                storeData(aSessionData(sessionId = "@alice:domain.com"))
            }
        )
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs")
    }

    @Test
    fun `when the log directory is updated, the tracing service is invoked`() = runTest {
        var param: WriteToFilesConfiguration? = null
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {
            param = it
        }
        val sut = createDefaultBugReporter(
            buildMeta = aBuildMeta(isEnterpriseBuild = true),
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            ),
        )
        sut.setLogDirectorySubfolder("my.sub.folder")
        updateWriteToFilesConfigurationResult.assertions().isCalledOnce()
        assertThat(param).isNotNull()
        assertThat(param).isInstanceOf(WriteToFilesConfiguration.Enabled::class.java)
        assertThat((param as WriteToFilesConfiguration.Enabled).directory).endsWith("/cache/logs/my.sub.folder")
        assertThat((param as WriteToFilesConfiguration.Enabled).filenamePrefix).isEqualTo("logs")
        assertThat((param as WriteToFilesConfiguration.Enabled).numberOfFiles).isEqualTo(168)
        assertThat((param as WriteToFilesConfiguration.Enabled).filenameSuffix).isEqualTo("log")
    }

    @Test
    fun `foss build - when the log directory is updated, the tracing service is not invoked`() = runTest {
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {}
        val sut = createDefaultBugReporter(
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            )
        )
        sut.setLogDirectorySubfolder("my.sub.folder")
        updateWriteToFilesConfigurationResult.assertions().isNeverCalled()
    }

    @Test
    fun `when the log directory is reset, the tracing service is invoked`() = runTest {
        var param: WriteToFilesConfiguration? = null
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {
            param = it
        }
        val sut = createDefaultBugReporter(
            buildMeta = aBuildMeta(isEnterpriseBuild = true),
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            ),
        )
        sut.setLogDirectorySubfolder(null)
        updateWriteToFilesConfigurationResult.assertions().isCalledOnce()
        assertThat(param).isNotNull()
        assertThat(param).isInstanceOf(WriteToFilesConfiguration.Enabled::class.java)
        assertThat((param as WriteToFilesConfiguration.Enabled).directory).endsWith("/cache/logs")
        assertThat((param as WriteToFilesConfiguration.Enabled).filenamePrefix).isEqualTo("logs")
        assertThat((param as WriteToFilesConfiguration.Enabled).numberOfFiles).isEqualTo(168)
        assertThat((param as WriteToFilesConfiguration.Enabled).filenameSuffix).isEqualTo("log")
    }

    @Test
    fun `foss build - when the log directory is reset, the tracing service is not invoked`() = runTest {
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {}
        val sut = createDefaultBugReporter(
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            )
        )
        sut.setLogDirectorySubfolder(null)
        updateWriteToFilesConfigurationResult.assertions().isNeverCalled()
    }

    @Test
    fun `when a new MatrixClient is created the logs folder is updated`() = runTest {
        var param: WriteToFilesConfiguration? = null
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {
            param = it
        }
        val matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
            givenMatrixClient(
                FakeMatrixClient(
                    userIdServerNameLambda = { "domain.foo.org" },
                )
            )
        }
        val sut = createDefaultBugReporter(
            buildMeta = aBuildMeta(isEnterpriseBuild = true),
            matrixAuthenticationService = matrixAuthenticationService,
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            )
        )
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs")
        matrixAuthenticationService.login("alice", "password")
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs/domain.foo.org")
        updateWriteToFilesConfigurationResult.assertions().isCalledOnce()
        assertThat(param).isNotNull()
        assertThat(param).isInstanceOf(WriteToFilesConfiguration.Enabled::class.java)
        assertThat((param as WriteToFilesConfiguration.Enabled).directory).endsWith("/cache/logs/domain.foo.org")
        assertThat((param as WriteToFilesConfiguration.Enabled).filenamePrefix).isEqualTo("logs")
        assertThat((param as WriteToFilesConfiguration.Enabled).numberOfFiles).isEqualTo(168)
        assertThat((param as WriteToFilesConfiguration.Enabled).filenameSuffix).isEqualTo("log")
    }

    @Test
    fun `foss build - when a new MatrixClient is created the logs folder is not updated`() = runTest {
        val updateWriteToFilesConfigurationResult = lambdaRecorder<WriteToFilesConfiguration, Unit> {}
        val matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
            givenMatrixClient(
                FakeMatrixClient(
                    userIdServerNameLambda = { "domain.foo.org" },
                )
            )
        }
        val sut = createDefaultBugReporter(
            matrixAuthenticationService = matrixAuthenticationService,
            tracingService = FakeTracingService(
                updateWriteToFilesConfigurationResult = updateWriteToFilesConfigurationResult,
            )
        )
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs")
        matrixAuthenticationService.login("alice", "password")
        assertThat(sut.logDirectory().absolutePath).endsWith("/cache/logs")
        updateWriteToFilesConfigurationResult.assertions().isNeverCalled()
    }

    private fun TestScope.createDefaultBugReporter(
        buildMeta: BuildMeta = aBuildMeta(),
        sessionStore: SessionStore = InMemorySessionStore(),
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider(),
        crashDataStore: CrashDataStore = FakeCrashDataStore(),
        server: MockWebServer = MockWebServer(),
        tracingService: TracingService = FakeTracingService(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
    ): DefaultBugReporter {
        return DefaultBugReporter(
            context = RuntimeEnvironment.getApplication(),
            screenshotHolder = FakeScreenshotHolder(),
            crashDataStore = crashDataStore,
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta, FakeSdkMetadata("123456789")),
            sessionStore = sessionStore,
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
            matrixClientProvider = matrixClientProvider,
            tracingService = tracingService,
            matrixAuthenticationService = matrixAuthenticationService,
        )
    }

    companion object {
        private const val EXPECTED_NUMBER_OF_PROGRESS_VALUE = 17
    }
}
