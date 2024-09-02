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

package io.element.android.features.licenses.impl.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
) : Parcelable {
    @IgnoredOnParcel
    val safeName = name?.takeIf { name -> name != "null" } ?: "$groupId:$artifactId"
}

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
