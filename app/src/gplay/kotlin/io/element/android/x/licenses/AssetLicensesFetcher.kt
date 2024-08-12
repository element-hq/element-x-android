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

package io.element.android.x.licenses

import android.content.Context
import android.os.Parcelable
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import javax.inject.Inject

class AssetLicensesFetcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun fetchLicenses(): List<DependencyLicenseItem> {
        return withContext(dispatchers.io) {
            tryOrNull(
                onError = {
                    Timber.e(it, "Failed to fetch licenses from assets")
                }
            ) {
                context.assets.open("licensee-artifacts.json").use { inputStream ->
                    val json = Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                    json.decodeFromStream<List<DependencyLicenseItem>?>(inputStream)
                        ?.map {
                            if (it.name == null || it.name == "null") {
                                it.copy(name = "${it.groupId}:${it.artifactId}")
                            } else {
                                it
                            }
                        }
                        ?.sortedBy { it.name?.lowercase() }
                }
            } ?: emptyList()
        }
    }
}

@Serializable
@Parcelize
data class DependencyLicenseItem(
    val groupId: String,
    val artifactId: String,
    val version: String,
    @SerialName("spdxLicenses")
    val licenses: List<License>?,
    val unknownLicenses: List<License>?,
    val name: String?,
    val scm: Scm?,
) : Parcelable

@Serializable
@Parcelize
data class License(
    val identifier: String?,
    val name: String?,
    val url: String?,
) : Parcelable

@Serializable
@Parcelize
data class Scm(
    val url: String,
) : Parcelable
