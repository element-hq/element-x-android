/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.a11y

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.element.android.features.messages.impl.R
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
@ReadOnlyComposable
fun a11yReactionAction(
    emoji: String,
    userAlreadyReacted: Boolean,
): String {
    return if (userAlreadyReacted) {
        stringResource(id = CommonStrings.a11y_remove_reaction_with, emoji)
    } else {
        stringResource(id = CommonStrings.a11y_react_with, emoji)
    }
}

@Composable
@ReadOnlyComposable
fun a11yReactionDetails(
    emoji: String,
    userAlreadyReacted: Boolean,
    reactionCount: Int,
): String {
    val reaction = if (emoji.startsWith("mxc://")) {
        stringResource(CommonStrings.common_an_image)
    } else {
        emoji
    }
    return if (userAlreadyReacted) {
        if (reactionCount == 1) {
            stringResource(R.string.screen_room_timeline_reaction_you_a11y, reaction)
        } else {
            pluralStringResource(
                R.plurals.screen_room_timeline_reaction_including_you_a11y,
                reactionCount - 1,
                reactionCount - 1,
                reaction,
            )
        }
    } else {
        pluralStringResource(
            R.plurals.screen_room_timeline_reaction_a11y,
            reactionCount,
            reactionCount,
            reaction,
        )
    }
}
