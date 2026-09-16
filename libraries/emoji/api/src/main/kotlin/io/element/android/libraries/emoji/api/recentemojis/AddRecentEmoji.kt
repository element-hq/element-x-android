/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.api.recentemojis

/**
 * Records that an emoji was just used, so it surfaces in the picker's recent list; see [GetRecentEmojis] for the read side.
 */
fun interface AddRecentEmoji {
    /**
     * @param emoji the emoji that was just used, as its unicode representation.
     */
    suspend operator fun invoke(emoji: String): Result<Unit>
}
