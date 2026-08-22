/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.install

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.privatepush.impl.system.FakeInstalledAppsDetector
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NtfyInstallerTest {
    private fun TestScope.createInstaller(
        downloader: FakeApkDownloader = FakeApkDownloader(),
        fetcher: FakeNtfyManifestFetcher = FakeNtfyManifestFetcher(),
        detector: FakeInstalledAppsDetector = FakeInstalledAppsDetector(),
    ): NtfyInstaller {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return NtfyInstaller(
            manifestFetcher = fetcher,
            apkDownloader = downloader,
            installedAppsDetector = detector,
            appCoroutineScope = this,
            coroutineDispatchers = CoroutineDispatchers(io = dispatcher, computation = dispatcher, main = dispatcher),
        )
    }

    @Test
    fun `happy path downloads the pinned request and offers auto-install once`() = runTest {
        val downloader = FakeApkDownloader()
        val installer = createInstaller(downloader = downloader, detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 60L)))
        assertThat(installer.step.value).isEqualTo(AppUpdateStep.Idle)
        installer.startDownload()
        advanceUntilIdle()
        assertThat(installer.step.value).isEqualTo(AppUpdateStep.ReadyToInstall(FakeApkDownloader.APK_PATH))
        assertThat(downloader.requests.single().minVersionCodeExclusive).isEqualTo(60L)
        assertThat(downloader.requests.single().signingCertSha256).isEqualTo(PrivatePushConfig.NTFY_SIGNING_CERT_SHA256)
        assertThat(installer.consumePendingAutoInstall()).isEqualTo(FakeApkDownloader.APK_PATH)
        assertThat(installer.consumePendingAutoInstall()).isNull()
    }

    @Test
    fun `a missing or rejected manifest fails without downloading`() = runTest {
        val downloader = FakeApkDownloader()
        val installer = createInstaller(downloader = downloader, fetcher = FakeNtfyManifestFetcher(result = null))
        installer.startDownload()
        advanceUntilIdle()
        assertThat(installer.step.value).isEqualTo(AppUpdateStep.Failed)
        assertThat(downloader.requests).isEmpty()

        val rejected = createInstaller(downloader = downloader, fetcher = FakeNtfyManifestFetcher(result = aNtfyManifest(packageName = "com.evil")))
        rejected.startDownload()
        advanceUntilIdle()
        assertThat(rejected.step.value).isEqualTo(AppUpdateStep.Failed)
        assertThat(downloader.requests).isEmpty()
    }

    @Test
    fun `install only opens the installer when ready, cancelAndReset deletes the ntfy file`() = runTest {
        val downloader = FakeApkDownloader()
        val installer = createInstaller(downloader = downloader)
        assertThat(installer.install()).isFalse()
        installer.startDownload()
        advanceUntilIdle()
        assertThat(installer.install()).isTrue()
        assertThat(downloader.installed).containsExactly(FakeApkDownloader.APK_PATH)

        installer.cancelAndReset()
        advanceUntilIdle()
        assertThat(installer.step.value).isEqualTo(AppUpdateStep.Idle)
        assertThat(installer.pendingAutoInstall.value).isNull()
        assertThat(downloader.deletedFiles).containsExactly(NTFY_APK_FILE_NAME)
    }
}
