/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.animation.core.animateDpAsState
import io.element.android.libraries.designsystem.animation.M3Motion
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.element.android.libraries.textcomposer.model.MessageComposerMode

@Composable
internal fun textInputRoundedCornerShape(
    composerMode: MessageComposerMode,
): RoundedCornerShape {
    val roundCornerSmall = 28.dp
    val roundCornerLarge = 28.dp

    val roundedCornerSize = if (composerMode is MessageComposerMode.Special) {
        roundCornerSmall
    } else {
        roundCornerLarge
    }

    val roundedCornerSizeState = animateDpAsState(
        targetValue = roundedCornerSize,
        animationSpec = M3Motion.defaultValueSpec(),
        label = "roundedCornerSizeAnimation"
    )
    return RoundedCornerShape(roundedCornerSizeState.value)
}
