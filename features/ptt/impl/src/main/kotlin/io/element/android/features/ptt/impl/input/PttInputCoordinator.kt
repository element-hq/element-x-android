/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.input

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttInputSource
import io.element.android.features.ptt.api.PttInputSourceFactory
import io.element.android.features.ptt.api.PttInputSourceId
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.libraries.di.annotations.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Owns the set of hardware/accessory [PttInputSource]s for the life of a PTT session. Started and
 * stopped by [io.element.android.features.ptt.impl.services.PttSessionHostService].
 *
 * Every source's press/release is funnelled to [PttSessionController.pressToTalk]/[releaseToTalk]
 * through a shared arbitration guard, so a physical key that surfaces via two sources (e.g. the same
 * accessory reaching both MediaSession and Activity key dispatch) can't double-fire: the first down
 * starts transmitting and the first up stops it, later duplicates are ignored.
 */
@SingleIn(AppScope::class)
@Inject
class PttInputCoordinator(
    @ApplicationContext private val context: Context,
    private val factories: @JvmSuppressWildcards Map<PttInputSourceId, PttInputSourceFactory>,
    private val sessionManager: PttSessionManager,
) {
    private val activeSources = mutableListOf<PttInputSource>()
    private val transmitting = AtomicBoolean(false)
    private var started = false

    private fun onPressToTalk() {
        // First press wins; ignore duplicate downs (key repeat, or the same key via another source).
        if (transmitting.compareAndSet(false, true)) {
            sessionManager.pressToTalk()
        }
    }

    private fun onReleaseToTalk() {
        if (transmitting.compareAndSet(true, false)) {
            sessionManager.releaseToTalk()
        }
    }

    /** Start every currently-available source. Idempotent — a no-op if already started. */
    fun start() {
        if (started) return
        started = true
        startAvailableSources()
    }

    /**
     * Start any source that has become available since [start] but isn't running yet (e.g. after the
     * user grants Bluetooth permission for the BLE button). No-op if the session isn't started, or if
     * nothing new is available. Safe to call repeatedly (e.g. on app resume).
     */
    fun refresh() {
        if (!started) return
        startAvailableSources()
    }

    private fun startAvailableSources() {
        val activeIds = activeSources.mapTo(mutableSetOf()) { it.id }
        factories.values
            .filter { it.id !in activeIds && it.isAvailable(context) }
            .forEach { factory ->
                val source = factory.create(context)
                source.start(onPressToTalk = ::onPressToTalk, onReleaseToTalk = ::onReleaseToTalk)
                activeSources += source
            }
    }

    /** Stop and release every source. Idempotent. */
    fun stop() {
        activeSources.forEach { it.stop() }
        activeSources.clear()
        transmitting.set(false)
        started = false
    }
}
