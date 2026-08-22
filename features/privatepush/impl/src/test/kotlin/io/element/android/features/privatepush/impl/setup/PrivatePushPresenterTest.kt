/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.impl.FakePrivatePushService
import io.element.android.features.privatepush.impl.install.FakeApkDownloader
import io.element.android.features.privatepush.impl.install.FakeNtfyManifestFetcher
import io.element.android.features.privatepush.impl.install.NTFY_APK_FILE_NAME
import io.element.android.features.privatepush.impl.install.NtfyInstaller
import io.element.android.features.privatepush.impl.install.aNtfyManifest
import io.element.android.features.privatepush.impl.system.FakeExternalAppLauncher
import io.element.android.features.privatepush.impl.system.FakeInstalledAppsDetector
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivatePushPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val ntfyDistributor = Distributor(PrivatePushConfig.NTFY_PACKAGE, "ntfy")

    private class RecordingCallback : PrivatePushNode.Callback {
        val onDoneLambda = lambdaRecorder<Unit> { }
        val onLaterLambda = lambdaRecorder<Unit> { }
        val navigateToTroubleshootLambda = lambdaRecorder<Unit> { }
        override fun onDone() = onDoneLambda()
        override fun onLater() = onLaterLambda()
        override fun navigateToTroubleshoot() = navigateToTroubleshootLambda()
    }

    /** Molecule emits one state per snapshot write after a suspension point: wait for the interesting one. */
    private suspend fun ReceiveTurbine<PrivatePushState>.awaitState(predicate: (PrivatePushState) -> Boolean): PrivatePushState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }

    private fun aUnifiedPushProvider(
        distributors: List<Distributor> = listOf(ntfyDistributor),
        unregisterResult: (MatrixClient) -> Result<Unit> = { Result.success(Unit) },
    ) = FakePushProvider(
        name = "UnifiedPush",
        supportMultipleDistributors = true,
        distributors = distributors,
        unregisterWithResult = unregisterResult,
    )

    private fun TestScope.createPrivatePushPresenter(
        callback: RecordingCallback = RecordingCallback(),
        detector: FakeInstalledAppsDetector = FakeInstalledAppsDetector(),
        launcher: FakeExternalAppLauncher = FakeExternalAppLauncher(),
        clipboardHelper: FakeClipboardHelper = FakeClipboardHelper(),
        privatePushService: FakePrivatePushService = FakePrivatePushService(),
        pushService: FakePushService = FakePushService(),
        downloader: FakeApkDownloader = FakeApkDownloader(),
        manifestFetcher: FakeNtfyManifestFetcher = FakeNtfyManifestFetcher(),
    ): PrivatePushPresenter {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return PrivatePushPresenter(
            callback = callback,
            matrixClient = FakeMatrixClient(sessionId = A_SESSION_ID),
            pushService = pushService,
            privatePushService = privatePushService,
            installedAppsDetector = detector,
            externalAppLauncher = launcher,
            clipboardHelper = clipboardHelper,
            ntfyInstaller = NtfyInstaller(
                manifestFetcher = manifestFetcher,
                apkDownloader = downloader,
                installedAppsDetector = detector,
                appCoroutineScope = backgroundScope,
                coroutineDispatchers = CoroutineDispatchers(io = dispatcher, computation = dispatcher, main = dispatcher),
            ),
        )
    }

    @Test
    fun `initial state`() = runTest {
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.PLAY_STORE_PACKAGE to 1L))
        val presenter = createPrivatePushPresenter(detector = detector)
        presenter.test {
            val state = awaitItem()
            assertThat(state.page).isEqualTo(PrivatePushPage.Why)
            assertThat(state.serverAddress).isEqualTo(PrivatePushConfig.SERVER_URL)
            assertThat(state.ntfyInstalled).isFalse()
            assertThat(state.playStoreAvailable).isTrue()
            assertThat(state.fdroidAvailable).isFalse()
            assertThat(state.download).isEqualTo(AppUpdateStep.Idle)
            assertThat(state.connect).isEqualTo(ConnectState.Idle)
            assertThat(state.wrongServerHost).isNull()
            assertThat(state.addressCopied).isFalse()
        }
    }

    @Test
    fun `continue from Why goes to Install when ntfy is missing, to Configure when installed`() = runTest {
        createPrivatePushPresenter().test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            assertThat(awaitItem().page).isEqualTo(PrivatePushPage.Install)
        }
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(detector = detector).test {
            val state = awaitItem()
            assertThat(state.ntfyInstalled).isTrue()
            state.eventSink(PrivatePushEvents.Continue)
            assertThat(awaitItem().page).isEqualTo(PrivatePushPage.Configure)
        }
    }

    @Test
    fun `refresh on the Install page advances to Configure once ntfy is installed and resets the download`() = runTest {
        val detector = FakeInstalledAppsDetector()
        val downloader = FakeApkDownloader()
        createPrivatePushPresenter(detector = detector, downloader = downloader).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            val install = awaitItem()
            assertThat(install.page).isEqualTo(PrivatePushPage.Install)
            detector.installed[PrivatePushConfig.NTFY_PACKAGE] = 63L
            install.eventSink(PrivatePushEvents.Refresh)
            val detected = awaitItem()
            assertThat(detected.ntfyInstalled).isTrue()
            val configure = awaitItem()
            assertThat(configure.page).isEqualTo(PrivatePushPage.Configure)
            assertThat(downloader.deletedFiles).contains(NTFY_APK_FILE_NAME)
        }
    }

    @Test
    fun `download from Feral forwards the installer steps and auto-installs once`() = runTest {
        val downloader = FakeApkDownloader()
        createPrivatePushPresenter(downloader = downloader).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.DownloadFromFeral)
            assertThat(awaitItem().download).isEqualTo(AppUpdateStep.Downloading(percent = null))
            assertThat(awaitItem().download).isEqualTo(AppUpdateStep.Downloading(percent = 50))
            assertThat(awaitItem().download).isEqualTo(AppUpdateStep.ReadyToInstall(FakeApkDownloader.APK_PATH))
            // The pending auto-install is consumed once (two more recompositions, same state content).
            awaitState { it.download is AppUpdateStep.ReadyToInstall }
            runCurrent()
            assertThat(downloader.installed).containsExactly(FakeApkDownloader.APK_PATH)
            assertThat(downloader.requests.single().packageName).isEqualTo(PrivatePushConfig.NTFY_PACKAGE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `download is rejected when the manifest describes another package`() = runTest {
        val downloader = FakeApkDownloader()
        val fetcher = FakeNtfyManifestFetcher(result = aNtfyManifest(packageName = "com.evil"))
        createPrivatePushPresenter(downloader = downloader, manifestFetcher = fetcher).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.DownloadFromFeral)
            assertThat(awaitItem().download).isEqualTo(AppUpdateStep.Downloading(percent = null))
            assertThat(awaitItem().download).isEqualTo(AppUpdateStep.Failed)
            assertThat(downloader.requests).isEmpty()
            assertThat(downloader.installed).isEmpty()
        }
    }

    @Test
    fun `store buttons open the stores`() = runTest {
        val launcher = FakeExternalAppLauncher()
        createPrivatePushPresenter(launcher = launcher).test {
            val state = awaitItem()
            state.eventSink(PrivatePushEvents.InstallFromPlayStore)
            state.eventSink(PrivatePushEvents.InstallFromFdroid)
            assertThat(launcher.playStoreRequests).containsExactly(PrivatePushConfig.NTFY_PACKAGE)
            assertThat(launcher.fdroidRequests).containsExactly(PrivatePushConfig.NTFY_PACKAGE)
        }
    }

    @Test
    fun `copy address puts the server URL on the clipboard and open ntfy launches it`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val launcher = FakeExternalAppLauncher()
        createPrivatePushPresenter(clipboardHelper = clipboardHelper, launcher = launcher).test {
            val state = awaitItem()
            state.eventSink(PrivatePushEvents.CopyAddress)
            assertThat(awaitItem().addressCopied).isTrue()
            assertThat(clipboardHelper.clipboardContents).isEqualTo(PrivatePushConfig.SERVER_URL)
            state.eventSink(PrivatePushEvents.OpenNtfy)
            assertThat(launcher.openedApps).containsExactly(PrivatePushConfig.NTFY_PACKAGE)
        }
    }

    @Test
    fun `activate registers with the ntfy distributor and reaches Done when the endpoint is private`() = runTest {
        val callback = RecordingCallback()
        val provider = aUnifiedPushProvider()
        val registerWithLambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val pushService = FakePushService(availablePushProviders = listOf(provider), registerWithLambda = registerWithLambda)
        val privatePushService = FakePrivatePushService(statusResult = PrivatePushStatus.Private)
        privatePushService.dismissed.value = true
        privatePushService.requestSetup(A_SESSION_ID)
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(
            callback = callback,
            detector = detector,
            pushService = pushService,
            privatePushService = privatePushService,
        ).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Continue)
            val connect = awaitItem()
            assertThat(connect.page).isEqualTo(PrivatePushPage.Connect)
            connect.eventSink(PrivatePushEvents.Activate)
            assertThat(awaitItem().connect).isEqualTo(ConnectState.Connecting)
            val done = awaitState { it.page == PrivatePushPage.Done }
            assertThat(done.connect).isEqualTo(ConnectState.Connected)
            registerWithLambda.assertions().isCalledOnce().with(any(), value(provider), value(ntfyDistributor))
            assertThat(privatePushService.dismissed.value).isFalse()
            assertThat(privatePushService.requests.value).isEmpty()
            done.eventSink(PrivatePushEvents.Finish)
            callback.onDoneLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `activate with an endpoint on ntfy_sh drops the stale registration, shows WrongServer and GoToConfigure carries the hint`() = runTest {
        val unregisterLambda = lambdaRecorder<MatrixClient, Result<Unit>> { Result.success(Unit) }
        val provider = aUnifiedPushProvider(unregisterResult = unregisterLambda)
        val pushService = FakePushService(availablePushProviders = listOf(provider))
        val privatePushService = FakePrivatePushService(statusResult = PrivatePushStatus.PublicServer("ntfy.sh"))
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(detector = detector, pushService = pushService, privatePushService = privatePushService).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Activate)
            assertThat(awaitItem().connect).isEqualTo(ConnectState.Connecting)
            val problem = awaitState { it.connect is ConnectState.Problem }
            assertThat(problem.connect).isEqualTo(ConnectState.Problem(ConnectProblem.WrongServer("ntfy.sh")))
            assertThat(problem.wrongServerHost).isEqualTo("ntfy.sh")
            unregisterLambda.assertions().isCalledOnce()
            problem.eventSink(PrivatePushEvents.GoToConfigure)
            val configure = awaitState { it.page == PrivatePushPage.Configure }
            assertThat(configure.connect).isEqualTo(ConnectState.Idle)
            assertThat(configure.wrongServerHost).isEqualTo("ntfy.sh")
        }
    }

    @Test
    fun `activate without the ntfy distributor shows NtfyNotInstalled and GoToInstall goes back`() = runTest {
        val provider = aUnifiedPushProvider(distributors = emptyList())
        val pushService = FakePushService(availablePushProviders = listOf(provider))
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(detector = detector, pushService = pushService).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Activate)
            // No suspension point before the verdict: Connecting and the problem may coalesce into one emission.
            val problem = awaitState { it.connect is ConnectState.Problem }
            assertThat(problem.connect).isEqualTo(ConnectState.Problem(ConnectProblem.NtfyNotInstalled))
            detector.installed.clear()
            problem.eventSink(PrivatePushEvents.GoToInstall)
            val install = awaitState { it.page == PrivatePushPage.Install }
            assertThat(install.connect).isEqualTo(ConnectState.Idle)
            // The Install page polls the detector and notices that ntfy is gone.
            assertThat(awaitState { !it.ntfyInstalled }.page).isEqualTo(PrivatePushPage.Install)
        }
    }

    @Test
    fun `a registration failure shows RegistrationFailed with the reason and troubleshoot is forwarded`() = runTest {
        val callback = RecordingCallback()
        val provider = aUnifiedPushProvider()
        val pushService = FakePushService(
            availablePushProviders = listOf(provider),
            registerWithLambda = { _, _, _ -> Result.failure(IllegalStateException("timeout")) },
        )
        val privatePushService = FakePrivatePushService(statusResult = PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected))
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(callback = callback, detector = detector, pushService = pushService, privatePushService = privatePushService).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Activate)
            assertThat(awaitItem().connect).isEqualTo(ConnectState.Connecting)
            val problem = awaitState { it.connect is ConnectState.Problem }
            assertThat(problem.connect).isEqualTo(ConnectState.Problem(ConnectProblem.RegistrationFailed("timeout")))
            problem.eventSink(PrivatePushEvents.OpenTroubleshoot)
            callback.navigateToTroubleshootLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `later persists the dismissal, clears the request and calls onLater`() = runTest {
        val callback = RecordingCallback()
        val privatePushService = FakePrivatePushService()
        privatePushService.requestSetup(A_SESSION_ID)
        createPrivatePushPresenter(callback = callback, privatePushService = privatePushService).test {
            awaitItem().eventSink(PrivatePushEvents.Later)
            runCurrent()
            assertThat(privatePushService.dismissed.value).isTrue()
            assertThat(privatePushService.requests.value).isEmpty()
            callback.onLaterLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `back from Why behaves like Later, back from Connect returns to Configure`() = runTest {
        val callback = RecordingCallback()
        val detector = FakeInstalledAppsDetector(mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L))
        createPrivatePushPresenter(callback = callback, detector = detector).test {
            awaitItem().eventSink(PrivatePushEvents.Continue)
            awaitItem().eventSink(PrivatePushEvents.Continue)
            val connect = awaitItem()
            assertThat(connect.page).isEqualTo(PrivatePushPage.Connect)
            connect.eventSink(PrivatePushEvents.Back)
            assertThat(awaitItem().page).isEqualTo(PrivatePushPage.Configure)
        }
        createPrivatePushPresenter(callback = callback, detector = detector).test {
            awaitItem().eventSink(PrivatePushEvents.Back)
            runCurrent()
            callback.onLaterLambda.assertions().isCalledOnce()
        }
    }
}
