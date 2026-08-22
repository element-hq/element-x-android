/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.privatepush.api.PrivatePushService
import io.element.android.features.privatepush.impl.install.NtfyInstaller
import io.element.android.features.privatepush.impl.system.ExternalAppLauncher
import io.element.android.features.privatepush.impl.system.InstalledAppsDetector
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the 5 pages of the private-notifications setup.
 *
 * ⚠ Runs inside Molecule: never read a CompositionLocal here. Everything needing a Context is
 * injected (launcher, detector, clipboard, installer, connector); ON_RESUME refreshes come from
 * the view as [PrivatePushEvents.Refresh]. Long-running work (download, registration) lives in
 * app-scoped owners so it survives the node leaving composition.
 */
@AssistedInject
class PrivatePushPresenter(
    @Assisted private val callback: PrivatePushNode.Callback,
    private val matrixClient: MatrixClient,
    private val pushService: PushService,
    private val privatePushService: PrivatePushService,
    private val installedAppsDetector: InstalledAppsDetector,
    private val externalAppLauncher: ExternalAppLauncher,
    private val clipboardHelper: ClipboardHelper,
    private val ntfyInstaller: NtfyInstaller,
    private val connector: PrivatePushConnector,
) : Presenter<PrivatePushState> {
    @AssistedFactory
    interface Factory {
        fun create(callback: PrivatePushNode.Callback): PrivatePushPresenter
    }

    @Composable
    override fun present(): PrivatePushState {
        val coroutineScope = rememberCoroutineScope()
        val sessionId = matrixClient.sessionId
        var page by remember { mutableStateOf(PrivatePushPage.Why) }
        var ntfyInstalled by remember { mutableStateOf(isNtfyInstalled()) }
        val playStoreAvailable = remember { installedAppsDetector.isInstalled(PrivatePushConfig.PLAY_STORE_PACKAGE) }
        val fdroidAvailable = remember { PrivatePushConfig.FDROID_PACKAGES.any(installedAppsDetector::isInstalled) }
        val download by ntfyInstaller.step.collectAsState()
        val pendingAutoInstall by ntfyInstaller.pendingAutoInstall.collectAsState()
        // A verdict reached before the sequence even starts (ntfy missing): kept out of the connector.
        var localConnectProblem by remember { mutableStateOf<ConnectState.Problem?>(null) }
        val connectorState by connector.state.collectAsState()
        val connect = localConnectProblem ?: connectorState
        val alreadyDismissed by remember { privatePushService.isDismissed(sessionId) }.collectAsState(initial = false)
        // A previous run's verdict must not leak into a fresh flow (a running one is kept).
        remember { connector.reset() }
        var wrongServerHost by remember { mutableStateOf<String?>(null) }
        var addressCopied by remember { mutableStateOf(false) }

        // Auto-detect: the member installs ntfy from a store / the system installer and comes back.
        LaunchedEffect(page) {
            if (page != PrivatePushPage.Install) return@LaunchedEffect
            while (isActive) {
                ntfyInstalled = isNtfyInstalled()
                if (ntfyInstalled) {
                    ntfyInstaller.cancelAndReset()
                    page = PrivatePushPage.Configure
                    return@LaunchedEffect
                }
                delay(PrivatePushConfig.INSTALL_POLL_INTERVAL_MS)
            }
        }

        // A finished download opens the system installer once.
        LaunchedEffect(pendingAutoInstall) {
            if (pendingAutoInstall != null && ntfyInstaller.consumePendingAutoInstall() != null) {
                ntfyInstaller.install()
            }
        }

        // The connection sequence runs app-scoped (never cancelled with the node); mirror its
        // verdict into the pages. Connected moves to Done whatever the current page, so a member
        // coming back after a notification tap still lands on the confirmation.
        LaunchedEffect(connect) {
            when (val current = connect) {
                ConnectState.Connected -> page = PrivatePushPage.Done
                is ConnectState.Problem -> {
                    val problem = current.problem
                    if (problem is ConnectProblem.WrongServer) wrongServerHost = problem.host
                }
                ConnectState.Idle,
                ConnectState.Connecting -> Unit
            }
        }

        fun activate() {
            ntfyInstalled = isNtfyInstalled()
            val match = if (ntfyInstalled) findNtfyDistributor() else null
            if (match == null) {
                localConnectProblem = ConnectState.Problem(ConnectProblem.NtfyNotInstalled)
                return
            }
            localConnectProblem = null
            val (provider, distributor) = match
            connector.connect(matrixClient, provider, distributor)
        }

        fun dontAskAgain() = coroutineScope.launch {
            ntfyInstaller.cancelAndReset()
            pushService.setIgnoreRegistrationError(sessionId, true)
            privatePushService.setDismissed(sessionId, true)
            privatePushService.clearSetupRequest(sessionId)
            callback.onLater()
        }

        fun later() = coroutineScope.launch {
            ntfyInstaller.cancelAndReset()
            privatePushService.setDismissed(sessionId, true)
            privatePushService.clearSetupRequest(sessionId)
            callback.onLater()
        }

        fun clearConnectVerdict() {
            localConnectProblem = null
            connector.reset()
        }

        fun handleEvents(event: PrivatePushEvents) {
            when (event) {
                PrivatePushEvents.Continue -> page = when (page) {
                    PrivatePushPage.Why -> if (ntfyInstalled) PrivatePushPage.Configure else PrivatePushPage.Install
                    PrivatePushPage.Install -> PrivatePushPage.Configure
                    PrivatePushPage.Configure -> PrivatePushPage.Connect
                    PrivatePushPage.Connect,
                    PrivatePushPage.Done -> page
                }
                PrivatePushEvents.Back -> when (page) {
                    PrivatePushPage.Why -> later()
                    PrivatePushPage.Install -> page = PrivatePushPage.Why
                    PrivatePushPage.Configure -> page = if (ntfyInstalled) PrivatePushPage.Why else PrivatePushPage.Install
                    PrivatePushPage.Connect -> {
                        clearConnectVerdict()
                        page = PrivatePushPage.Configure
                    }
                    PrivatePushPage.Done -> Unit
                }
                PrivatePushEvents.Later -> later()
                PrivatePushEvents.InstallFromPlayStore -> externalAppLauncher.openPlayStore(PrivatePushConfig.NTFY_PACKAGE)
                PrivatePushEvents.InstallFromFdroid -> externalAppLauncher.openFdroid(PrivatePushConfig.NTFY_PACKAGE)
                PrivatePushEvents.DownloadFromFeral -> ntfyInstaller.startDownload()
                PrivatePushEvents.InstallDownloaded -> ntfyInstaller.install()
                PrivatePushEvents.CopyAddress -> {
                    clipboardHelper.copyPlainText(PrivatePushConfig.SERVER_URL)
                    addressCopied = true
                }
                PrivatePushEvents.OpenNtfy -> {
                    if (!externalAppLauncher.openApp(PrivatePushConfig.NTFY_PACKAGE)) {
                        ntfyInstalled = isNtfyInstalled()
                    }
                }
                PrivatePushEvents.Activate -> activate()
                PrivatePushEvents.GoToInstall -> {
                    clearConnectVerdict()
                    page = PrivatePushPage.Install
                }
                PrivatePushEvents.GoToConfigure -> {
                    clearConnectVerdict()
                    addressCopied = false
                    page = PrivatePushPage.Configure
                }
                PrivatePushEvents.DontAskAgain -> dontAskAgain()
                PrivatePushEvents.OpenTroubleshoot -> callback.navigateToTroubleshoot()
                PrivatePushEvents.Refresh -> ntfyInstalled = isNtfyInstalled()
                PrivatePushEvents.Finish -> callback.onDone()
            }
        }

        return PrivatePushState(
            page = page,
            serverAddress = PrivatePushConfig.SERVER_URL,
            ntfyInstalled = ntfyInstalled,
            playStoreAvailable = playStoreAvailable,
            fdroidAvailable = fdroidAvailable,
            download = download,
            connect = connect,
            wrongServerHost = wrongServerHost,
            addressCopied = addressCopied,
            canStopAsking = alreadyDismissed,
            eventSink = ::handleEvents,
        )
    }

    private fun isNtfyInstalled() = installedAppsDetector.isInstalled(PrivatePushConfig.NTFY_PACKAGE)

    /** The provider (UnifiedPush) that lists ntfy among its distributors, with that distributor. */
    private fun findNtfyDistributor(): Pair<PushProvider, Distributor>? {
        return pushService.getAvailablePushProviders().firstNotNullOfOrNull { provider ->
            provider.getDistributors()
                .firstOrNull { it.value == PrivatePushConfig.NTFY_PACKAGE }
                ?.let { provider to it }
        }
    }
}
