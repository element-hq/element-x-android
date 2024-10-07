/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
