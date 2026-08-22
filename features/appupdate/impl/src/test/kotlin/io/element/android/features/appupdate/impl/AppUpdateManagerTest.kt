/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AppUpdateConfig
import io.element.android.features.appupdate.api.ApkDownloadRequest
import io.element.android.features.appupdate.api.ApkDownloader
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.appupdate.api.anAvailableUpdate
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateManagerTest {
    private class FakeApkDownloader(
        private val outcome: AppUpdateStep = AppUpdateStep.ReadyToInstall(APK_PATH),
    ) : ApkDownloader {
        var subscriptions = 0
        val requests = mutableListOf<ApkDownloadRequest>()
        val deletedFiles = mutableListOf<String>()
        var deleteDownloadsCalls = 0
        var cleanupCalls = 0
        val installed = mutableListOf<String>()

        override fun downloadAndVerify(request: ApkDownloadRequest): Flow<AppUpdateStep> = flow {
            subscriptions++
            requests += request
            emit(AppUpdateStep.Downloading(percent = null))
            delay(DOWNLOAD_DURATION_MS)
            emit(AppUpdateStep.Downloading(percent = 50))
            delay(DOWNLOAD_DURATION_MS)
            emit(outcome)
        }

        override fun install(apkPath: String) {
            installed += apkPath
        }

        override fun delete(fileName: String) {
            deletedFiles += fileName
        }

        override fun deleteDownloads() {
            deleteDownloadsCalls++
        }

        override fun cleanupStaleDownloads() {
            cleanupCalls++
        }
    }

    private fun TestScope.createManager(downloader: ApkDownloader): AppUpdateManager {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return AppUpdateManager(
            apkDownloader = downloader,
            buildMeta = aBuildMeta(applicationId = APPLICATION_ID, versionCode = INSTALLED_VERSION_CODE),
            // The TestScope itself (not backgroundScope): advanceUntilIdle() ignores background work.
            appCoroutineScope = this,
            coroutineDispatchers = CoroutineDispatchers(io = dispatcher, computation = dispatcher, main = dispatcher),
        )
    }

    @Test
    fun `a download follows the downloader steps and ends ready to install, auto-install consumed once`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Idle)

        manager.startDownload(anAvailableUpdate())
        runCurrent()
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Downloading(percent = null))
        advanceTimeBy(DOWNLOAD_DURATION_MS + 1)
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Downloading(percent = 50))
        advanceUntilIdle()
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.ReadyToInstall(APK_PATH))

        assertThat(manager.pendingAutoInstall.value).isEqualTo(APK_PATH)
        assertThat(manager.consumePendingAutoInstall()).isEqualTo(APK_PATH)
        assertThat(manager.consumePendingAutoInstall()).isNull()
        assertThat(manager.pendingAutoInstall.value).isNull()
    }

    @Test
    fun `starting a download while one is running is a no-op`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        manager.startDownload(anAvailableUpdate())
        runCurrent()
        manager.startDownload(anAvailableUpdate())
        advanceUntilIdle()
        assertThat(downloader.subscriptions).isEqualTo(1)
    }

    @Test
    fun `cancelAndReset stops a running download, returns to idle and deletes the files`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        manager.startDownload(anAvailableUpdate())
        advanceTimeBy(DOWNLOAD_DURATION_MS + 1)
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Downloading(percent = 50))

        manager.cancelAndReset()
        advanceUntilIdle()
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Idle)
        assertThat(manager.pendingAutoInstall.value).isNull()
        assertThat(downloader.deletedFiles).containsExactly(AppUpdateManager.DOWNLOAD_FILE_NAME)
        assertThat(downloader.deleteDownloadsCalls).isEqualTo(0)
    }

    @Test
    fun `the self-update request pins the Feral certificate, our package and an anti-downgrade floor`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        manager.startDownload(anAvailableUpdate(versionCode = 42L, url = "https://feralisme.fr/x.apk", sha256 = "abcd"))
        advanceUntilIdle()
        assertThat(downloader.requests).containsExactly(
            ApkDownloadRequest(
                url = "https://feralisme.fr/x.apk",
                sha256 = "abcd",
                packageName = APPLICATION_ID,
                versionCode = 42L,
                signingCertSha256 = AppUpdateConfig.SIGNING_CERT_SHA256,
                fileName = AppUpdateManager.DOWNLOAD_FILE_NAME,
                minVersionCodeExclusive = INSTALLED_VERSION_CODE,
            )
        )
    }

    @Test
    fun `a failed download ends in Failed without a pending auto-install`() = runTest {
        val downloader = FakeApkDownloader(outcome = AppUpdateStep.Failed)
        val manager = createManager(downloader)
        manager.startDownload(anAvailableUpdate())
        advanceUntilIdle()
        assertThat(manager.step.value).isEqualTo(AppUpdateStep.Failed)
        assertThat(manager.pendingAutoInstall.value).isNull()
        // Failed is retryable.
        manager.startDownload(anAvailableUpdate())
        advanceUntilIdle()
        assertThat(downloader.subscriptions).isEqualTo(2)
    }

    @Test
    fun `install only opens the installer when an APK is ready`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        assertThat(manager.install()).isFalse()
        assertThat(downloader.installed).isEmpty()

        manager.startDownload(anAvailableUpdate())
        advanceUntilIdle()
        assertThat(manager.install()).isTrue()
        assertThat(downloader.installed).containsExactly(APK_PATH)
    }

    @Test
    fun `stale downloads are cleaned only while idle`() = runTest {
        val downloader = FakeApkDownloader()
        val manager = createManager(downloader)
        manager.cleanupStaleDownloads()
        advanceUntilIdle()
        assertThat(downloader.cleanupCalls).isEqualTo(1)

        manager.startDownload(anAvailableUpdate())
        runCurrent()
        manager.cleanupStaleDownloads()
        advanceUntilIdle()
        assertThat(downloader.cleanupCalls).isEqualTo(1)
    }

    private companion object {
        const val APK_PATH = "/cache/updates/feral-update.apk"
        const val DOWNLOAD_DURATION_MS = 100L
        const val APPLICATION_ID = "fr.feralisme.feral"
        const val INSTALLED_VERSION_CODE = 10L
    }
}
