/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

interface LicensesProvider {
    suspend fun provides(): List<DependencyLicenseItem>
}

@ContributesBinding(AppScope::class)
class AssetLicensesProvider @Inject constructor(
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
