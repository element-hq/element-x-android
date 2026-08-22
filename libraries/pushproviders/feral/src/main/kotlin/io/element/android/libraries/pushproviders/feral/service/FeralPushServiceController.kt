/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.pushproviders.feral.FeralPushStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushServiceController", LoggerTag.PushLoggerTag)

/** Starts and stops [FeralPushConnectionService]. */
interface FeralPushServiceController {
    /** Start (or poke) the foreground service. Failures are logged, never thrown: the next app-foreground retries. */
    fun ensureStarted()

    /** Start the service only when at least one session is registered with the Feral provider. */
    fun startIfRegistered(): Job

    fun stop()
}

@ContributesBinding(AppScope::class)
class DefaultFeralPushServiceController(
    @ApplicationContext private val context: Context,
    private val feralPushStore: FeralPushStore,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : FeralPushServiceController {
    override fun ensureStarted() {
        val intent = Intent(context, FeralPushConnectionService::class.java)
        // Throws ForegroundServiceStartNotAllowedException (API 31+) when the app is in the background
        // without an exemption: log it, FeralPushInitializer retries when the app comes to the foreground.
        runCatchingExceptions { ContextCompat.startForegroundService(context, intent) }
            .onSuccess { Timber.tag(loggerTag.value).d("Connection service start requested") }
            .onFailure { Timber.tag(loggerTag.value).w(it, "Unable to start the connection service now") }
    }

    override fun startIfRegistered(): Job = coroutineScope.launch {
        if (feralPushStore.registrations.first().isNotEmpty()) {
            ensureStarted()
        } else {
            Timber.tag(loggerTag.value).d("No Feral registration, not starting the connection service")
        }
    }

    override fun stop() {
        runCatchingExceptions { context.stopService(Intent(context, FeralPushConnectionService::class.java)) }
            .onFailure { Timber.tag(loggerTag.value).w(it, "Unable to stop the connection service") }
    }
}
