/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.ui

import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji

internal fun aEmojisSessionVerificationData(
    emojiList: List<VerificationEmoji> = aVerificationEmojiList(),
): SessionVerificationData {
    return SessionVerificationData.Emojis(emojiList)
}

internal fun aDecimalsSessionVerificationData(
    decimals: List<Int> = listOf(123, 456, 789),
): SessionVerificationData {
    return SessionVerificationData.Decimals(decimals)
}

private fun aVerificationEmojiList() = listOf(
    VerificationEmoji(number = 27, emoji = "🍕", description = "Pizza"),
    VerificationEmoji(number = 54, emoji = "🚀", description = "Rocket"),
    VerificationEmoji(number = 54, emoji = "🚀", description = "Rocket"),
    VerificationEmoji(number = 42, emoji = "📕", description = "Book"),
    VerificationEmoji(number = 48, emoji = "🔨", description = "Hammer"),
    VerificationEmoji(number = 48, emoji = "🔨", description = "Hammer"),
    VerificationEmoji(number = 63, emoji = "📌", description = "Pin"),
)
