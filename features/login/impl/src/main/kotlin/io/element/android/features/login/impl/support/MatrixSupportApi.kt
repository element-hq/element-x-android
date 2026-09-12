/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.support

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

internal interface MatrixSupportApi {
    @GET(".well-known/matrix/support")
    suspend fun getSupport(): MatrixSupport
}

@Serializable
internal data class MatrixSupport(
    @SerialName("contacts") val contacts: List<MatrixSupportContact>? = null,
    @SerialName("support_page") val supportPage: String? = null,
)

@Serializable
internal data class MatrixSupportContact(
    @SerialName("role") val role: String? = null,
    @SerialName("matrix_id") val matrixId: String? = null,
    @SerialName("email_address") val emailAddress: String? = null,
)
