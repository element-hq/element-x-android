/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.cachecleaner.api

import android.content.Context
import androidx.startup.Initializer
import io.element.android.libraries.architecture.bindings

class CacheCleanerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        context.bindings<CacheCleanerBindings>().cacheCleaner().clearCache()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
