/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.service

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import io.element.android.libraries.architecture.bindings

/**
 * App-start hook, discovered by androidx.startup from this module's manifest: every time the app
 * comes to the foreground (including the first start), (re)start the connection service when a
 * session is registered with the Feral provider, and let [io.element.android.libraries.pushproviders.feral.FeralPushFallback]
 * heal a session stuck on a provider without distributor (once per process per session, no UI involved).
 * Starting an already running service is a no-op, and being in the foreground is exactly when
 * startForegroundService is allowed.
 */
class FeralPushInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val bindings = context.bindings<FeralPushBindings>()
        val controller = bindings.feralPushServiceController()
        val fallback = bindings.feralPushFallback()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    controller.startIfRegistered()
                    fallback.healLatestSession()
                }
            }
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(ProcessLifecycleInitializer::class.java)
}
