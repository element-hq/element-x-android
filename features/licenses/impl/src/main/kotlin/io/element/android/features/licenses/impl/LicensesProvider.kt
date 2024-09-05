/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
