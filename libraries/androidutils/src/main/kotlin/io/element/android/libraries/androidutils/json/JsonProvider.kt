/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.json

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

/**
 * Provides a Json instance configured to ignore unknown keys.
 */
fun interface JsonProvider : Provider<Json>

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultJsonProvider : JsonProvider {
    private val json: Json by lazy { Json { ignoreUnknownKeys = true } }
    override fun invoke() = json
}
