/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushBootReceiver", LoggerTag.PushLoggerTag)

@ContributesTo(AppScope::class)
interface FeralPushBootReceiverBindings {
    fun feralPushServiceController(): FeralPushServiceController
}

/** After a reboot, bring the connection service back when a session is registered with the Feral provider. */
class FeralPushBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Timber.tag(loggerTag.value).i("Boot completed")
        val controller = context.bindings<FeralPushBootReceiverBindings>().feralPushServiceController()
        val pendingResult = goAsync()
        controller.startIfRegistered().invokeOnCompletion { pendingResult.finish() }
    }
}
