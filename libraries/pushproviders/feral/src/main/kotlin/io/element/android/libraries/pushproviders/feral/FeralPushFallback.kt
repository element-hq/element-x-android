/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushFallback", LoggerTag.PushLoggerTag)

/**
 * Silent switch to the built-in Feral provider when the stored provider has no usable distributor
 * (ntfy uninstalled or never configured), so a session never stays stuck on "No distributors available".
 */
interface FeralPushFallback {
    /**
     * Register the built-in provider for the session through [PushService.registerWith] (which unregisters
     * the previous provider and stores the new provider name). Serialised: concurrent callers wait, and a
     * session already registered with the built-in provider returns true without registering again.
     */
    suspend fun register(matrixClient: MatrixClient): Boolean

    /**
     * Self-healing at app start, independent of any UI: wait for the latest session to be verified and, if
     * its stored provider has no distributor, [register] the built-in one. One attempt per process per
     * session, except after a failed registration, which is retried at the next app foreground.
     */
    fun healLatestSession(): Job
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFeralPushFallback(
    private val pushService: PushService,
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : FeralPushFallback {
    private val registrationMutex = Mutex()
    private val healMutex = Mutex()
    private val healedSessions = mutableSetOf<SessionId>()

    override suspend fun register(matrixClient: MatrixClient): Boolean = registrationMutex.withLock {
        val sessionId = matrixClient.sessionId
        val builtIn = pushService.getAvailablePushProviders().firstOrNull { it.name == FeralPushConfig.NAME }
        if (builtIn == null) {
            Timber.tag(loggerTag.value).w("Built-in Feral push provider not available, cannot fall back")
            return false
        }
        val distributor = builtIn.getDistributors().firstOrNull() ?: return false
        val current = pushService.getCurrentPushProvider(sessionId)
        if (current?.name == FeralPushConfig.NAME && current.getPushConfig(sessionId) != null) {
            Timber.tag(loggerTag.value).d("Built-in Feral provider already registered, nothing to do")
            return true
        }
        pushService.registerWith(matrixClient, builtIn, distributor)
            .onSuccess { Timber.tag(loggerTag.value).i("Fell back to the built-in Feral push provider") }
            .onFailure { Timber.tag(loggerTag.value).w(it, "Unable to fall back to the built-in Feral push provider") }
            .isSuccess
    }

    override fun healLatestSession(): Job = coroutineScope.launch {
        val sessionId = sessionStore.getLatestSession()?.userId?.let(::SessionId) ?: return@launch
        val firstAttempt = healMutex.withLock { healedSessions.add(sessionId) }
        if (!firstAttempt) return@launch
        val matrixClient = matrixClientProvider.getOrRestore(sessionId).getOrElse {
            Timber.tag(loggerTag.value).w(it, "Unable to get the session, not checking the push provider")
            return@launch
        }
        matrixClient.sessionVerificationService.sessionVerifiedStatus.first { it == SessionVerifiedStatus.Verified }
        val current = pushService.getCurrentPushProvider(sessionId)
        when {
            current == null ->
                Timber.tag(loggerTag.value).d("No push provider stored yet, the built-in one will be picked at registration")
            current.name == FeralPushConfig.NAME ->
                Timber.tag(loggerTag.value).d("Built-in Feral provider already selected")
            current.getDistributors().isNotEmpty() ->
                Timber.tag(loggerTag.value).d("Stored push provider \"${current.name}\" has a distributor, keeping it")
            else -> {
                Timber.tag(loggerTag.value).i("Stored push provider \"${current.name}\" has no distributor, falling back to the built-in Feral provider")
                if (!register(matrixClient)) {
                    // Let the next app foreground try again (no tight loop: one attempt per foreground).
                    healMutex.withLock { healedSessions.remove(sessionId) }
                }
            }
        }
    }
}
