/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.user

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class MatrixUser(
    val userId: UserId,
    val displayName: String? = null,
    val avatarUrl: String? = null,
) : Parcelable
