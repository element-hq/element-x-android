/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MatrixHomeServerDetails(
    val url: String,
    val supportsPasswordLogin: Boolean,
    val supportsOidcLogin: Boolean,
) : Parcelable
