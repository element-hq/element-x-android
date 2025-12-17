/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.join

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface JoinRule {
    data object Public : JoinRule
    data object Private : JoinRule
    data object Knock : JoinRule
    data object Invite : JoinRule
    data class Restricted(val rules: ImmutableList<AllowRule>) : JoinRule
    data class KnockRestricted(val rules: ImmutableList<AllowRule>) : JoinRule
    data class Custom(val value: String) : JoinRule
}
