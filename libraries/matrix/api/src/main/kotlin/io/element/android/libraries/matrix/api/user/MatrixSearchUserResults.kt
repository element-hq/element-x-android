/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.user

import kotlinx.collections.immutable.ImmutableList

data class MatrixSearchUserResults(
    val results: ImmutableList<MatrixUser>,
    val limited: Boolean,
)
