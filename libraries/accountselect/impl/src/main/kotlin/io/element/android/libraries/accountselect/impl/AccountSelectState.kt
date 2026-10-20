/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class AccountSelectState(
    /**
     * The text of the pill representing the link to open, or null if the link must not be rendered.
     */
    val linkPillText: String?,
    val accounts: ImmutableList<MatrixUser>,
)
