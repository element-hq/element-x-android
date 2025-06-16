/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.a11y

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
@ReadOnlyComposable
fun a11yReactionAction(
    emoji: String,
    userAlreadyReacted: Boolean = false,
): String {
    return if (userAlreadyReacted) {
        stringResource(id = CommonStrings.a11y_remove_reaction_with, emoji)
    } else {
        stringResource(id = CommonStrings.a11y_react_with, emoji)
    }
}
