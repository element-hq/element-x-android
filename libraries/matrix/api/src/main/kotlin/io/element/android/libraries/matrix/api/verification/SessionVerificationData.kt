/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SessionVerificationData {
    data class Emojis(
        // 7 emojis
        val emojis: List<VerificationEmoji>,
    ) : SessionVerificationData

    data class Decimals(
        // 3 numbers
        val decimals: List<Int>,
    ) : SessionVerificationData
}

// https://spec.matrix.org/unstable/client-server-api/#sas-method-emoji
data class VerificationEmoji(
    val number: Int,
    val emoji: String,
    val description: String,
)
