/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import io.element.android.features.rageshake.impl.crash.VectorUncaughtExceptionHandler

class CrashInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        VectorUncaughtExceptionHandler(context).activate()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
