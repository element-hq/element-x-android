/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import io.element.android.features.ptt.impl.PttAlwaysOnBindings
import io.element.android.libraries.architecture.bindings

/**
 * Kicks off the always-on PTT presence at app start: the controller then keeps the foreground
 * session host running while any room has PTT enabled. No-ops on builds without a PTT transport.
 */
class PttAlwaysOnInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        context.bindings<PttAlwaysOnBindings>().pttAlwaysOnController().start()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
