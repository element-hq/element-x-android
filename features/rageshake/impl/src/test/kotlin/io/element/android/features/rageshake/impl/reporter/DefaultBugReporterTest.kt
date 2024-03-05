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

import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.test.crash.FakeCrashDataStore
import io.element.android.features.rageshake.test.screenshot.FakeScreenshotHolder
import io.element.android.libraries.matrix.test.FakeSdkMetadata
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.network.useragent.DefaultUserAgentProvider
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
            theBugDescription = "a bug occurred",
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
            theBugDescription = "a bug occurred",
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
            coroutineScope = this,
            systemClock = FakeSystemClock(),
            coroutineDispatchers = testCoroutineDispatchers(),
            okHttpClient = { OkHttpClient.Builder().build() },
            userAgentProvider = DefaultUserAgentProvider(buildMeta),
            sessionStore = InMemorySessionStore(),
            buildMeta = buildMeta,
            bugReporterUrlProvider = { server.url("/") },
            sdkMetadata = FakeSdkMetadata("123456789"),
        )
    }

    companion object {
        private const val EXPECTED_NUMBER_OF_PROGRESS_VALUE = 15
    }
}
