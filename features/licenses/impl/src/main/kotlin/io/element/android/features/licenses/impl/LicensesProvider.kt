/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

interface LicensesProvider {
    suspend fun provides(): List<DependencyLicenseItem>
}

@ContributesBinding(AppScope::class)
class AssetLicensesProvider(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
) : LicensesProvider {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun provides(): List<DependencyLicenseItem> {
        return withContext(dispatchers.io) {
            context.assets.open("licensee-artifacts.json").use { inputStream ->
                val json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
                json.decodeFromStream<List<DependencyLicenseItem>>(inputStream)
                    .sortedBy { it.safeName.lowercase() }
            }
        }
    }
}
