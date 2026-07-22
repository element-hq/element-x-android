/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttEnabledSource
import io.element.android.features.ptt.api.PttTransportFactory
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.impl.services.PttSessionHostService
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

private val loggerTag = LoggerTag("PttAlwaysOn")

/**
 * Drives the always-on PTT presence: the foreground [PttSessionHostService] runs (connected, but
 * silent/warm — see the audio-output gate) whenever **≥1 room has PTT enabled** across any logged-in
 * session, and stops when the last one is disabled. This is what lets a user enable PTT on a room,
 * close the app, and keep receiving — the way Zello and similar radios stay connected in the
 * background.
 *
 * The service lifecycle is reconciled against the live session so an external teardown (e.g. an
 * explicit "Leave" while rooms are still enabled) self-heals back to connected.
 *
 * No-ops on builds without a real transport (FOSS, where the Mumble factory isn't contributed), so
 * it never spins up an empty foreground service.
 */
@SingleIn(AppScope::class)
@Inject
class PttAlwaysOnController(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val sessionStore: SessionStore,
    private val pttEnabledSource: PttEnabledSource,
    private val sessionController: PttSessionController,
    factories: @JvmSuppressWildcards Map<PttTransportType, PttTransportFactory>,
) {
    private val transportAvailable = factories.containsKey(PttTransportType.Mumble)
    private val started = AtomicBoolean(false)

    /** Start observing enabled rooms and driving the service. Idempotent; call once at app start. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (!transportAvailable) {
            Timber.tag(loggerTag.value).d("No PTT transport in this build; always-on service disabled")
            return
        }
        if (!started.compareAndSet(false, true)) return
        coroutineScope.launch {
            val targetFlow = sessionStore.sessionsFlow()
                .flatMapLatest { sessions -> enabledRoomsAcross(sessions.map { SessionId(it.userId) }) }
                .map { it.firstOrNull() }
            // Reconcile the desired target against whether a session is actually live, so a dropped
            // or externally-stopped session is brought back while rooms remain enabled.
            combine(targetFlow, sessionController.sessionState) { target, session ->
                target to (session != null)
            }
                .distinctUntilChanged()
                .collect { (target, isLive) ->
                    when {
                        target != null && !isLive -> startService(target)
                        target == null && isLive -> stopService()
                    }
                }
        }
    }

    private fun enabledRoomsAcross(sessionIds: List<SessionId>): Flow<List<Pair<SessionId, RoomId>>> {
        if (sessionIds.isEmpty()) return flowOf(emptyList())
        val perSession = sessionIds.map { sessionId ->
            pttEnabledSource.enabledRoomIdsFlow(sessionId).map { rooms -> rooms.map { sessionId to it } }
        }
        return combine(perSession) { lists -> lists.toList().flatten() }
    }

    private fun startService(target: Pair<SessionId, RoomId>) {
        Timber.tag(loggerTag.value).d("Enabled room present; starting always-on PTT service")
        try {
            PttSessionHostService.start(context, interimMumbleConfig(target.first, target.second))
        } catch (error: Exception) {
            // e.g. a background foreground-service start restriction — reboot/background auto-start is
            // handled separately (BOOT_COMPLETED is a later phase); don't crash app startup.
            Timber.tag(loggerTag.value).w(error, "Could not start always-on PTT service")
        }
    }

    private fun stopService() {
        Timber.tag(loggerTag.value).d("No rooms enabled; stopping always-on PTT service")
        PttSessionHostService.hangup(context)
    }
}
