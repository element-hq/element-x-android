/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.api.recentemojis

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Returns the list of recently used emojis for reactions.
 */
fun interface GetRecentEmojis {
    /** Returns the recently used emojis, most recent first; see [EmptyGetRecentEmojis] for a picker with no recent tab. */
    suspend operator fun invoke(): Result<ImmutableList<String>>
}

/**
 * A [GetRecentEmojis] that never returns any recent emojis. Handy for pickers that don't want
 * a "Recent" tab (e.g. the user-status picker).
 */
object EmptyGetRecentEmojis : GetRecentEmojis {
    override suspend fun invoke(): Result<ImmutableList<String>> = Result.success(persistentListOf())
}
