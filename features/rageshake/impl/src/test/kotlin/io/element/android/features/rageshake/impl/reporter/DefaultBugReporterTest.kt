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
import io.element.android.features.rageshake.impl.crash.FakeCrashDataStore
import io.element.android.features.rageshake.impl.screenshot.FakeScreenshotHolder
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.network.useragent.DefaultUserAgentProvider
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
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
        val sut = createDefaultBugReporter(server)
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

        val buildMeta = aBuildMeta()
        val fakeEncryptionService = FakeEncryptionService()
        val matrixClient = FakeMatrixClient(encryptionService = fakeEncryptionService)

        fakeEncryptionService.givenDeviceKeys("CURVECURVECURVE", "EDKEYEDKEYEDKY")
        val sut = DefaultBugReporter(
            context = RuntimeEnvironment.getApplication(),
            screenshotHolder = FakeScreenshotHolder(),
            crashDataStore = FakeCrashDataStore(),
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta, FakeSdkMetadata("123456789")),
            sessionStore = mockSessionStore,
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
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

        val buildMeta = aBuildMeta()
        val fakeEncryptionService = FakeEncryptionService()
        val matrixClient = FakeMatrixClient(encryptionService = fakeEncryptionService)

        fakeEncryptionService.givenDeviceKeys(null, null)
        val sut = DefaultBugReporter(
            context = RuntimeEnvironment.getApplication(),
            screenshotHolder = FakeScreenshotHolder(),
            crashDataStore = FakeCrashDataStore(),
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta, FakeSdkMetadata("123456789")),
            sessionStore = mockSessionStore,
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
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

        val buildMeta = aBuildMeta()
        val fakeEncryptionService = FakeEncryptionService()

        fakeEncryptionService.givenDeviceKeys(null, null)
        val sut = DefaultBugReporter(
            context = RuntimeEnvironment.getApplication(),
            screenshotHolder = FakeScreenshotHolder(),
            crashDataStore = FakeCrashDataStore("I did crash", true),
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta, FakeSdkMetadata("123456789")),
            sessionStore = InMemorySessionStore(),
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
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
        val sut = createDefaultBugReporter(server)
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

    private fun TestScope.createDefaultBugReporter(
        server: MockWebServer
    ): DefaultBugReporter {
        val buildMeta = aBuildMeta()
        return DefaultBugReporter(
            context = RuntimeEnvironment.getApplication(),
            screenshotHolder = FakeScreenshotHolder(),
            crashDataStore = FakeCrashDataStore(),
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta, FakeSdkMetadata("123456789")),
            sessionStore = InMemorySessionStore(),
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
            matrixClientProvider = FakeMatrixClientProvider()
        )
    }

    companion object {
        private const val EXPECTED_NUMBER_OF_PROGRESS_VALUE = 17
    }
}
