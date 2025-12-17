/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconColorButton
import io.element.android.libraries.designsystem.theme.components.IconColorButtonStyle

@Composable
internal fun DisabledComposerView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconColorButton(
            onClick = {},
            imageVector = CompoundIcons.Plus(),
            contentDescription = null,
            iconColorButtonStyle = IconColorButtonStyle.Disabled,
        )

        val bgColor = ElementTheme.colors.bgCanvasDisabled
        val borderColor = ElementTheme.colors.borderDisabled

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(21.dp))
                .border(0.5.dp, borderColor, RoundedCornerShape(21.dp))
                .background(color = bgColor)
                .size(42.dp)
                .requiredHeightIn(min = 42.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(48.dp),
            enabled = false,
            onClick = {},
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = CompoundIcons.SendSolid(),
                contentDescription = "",
                tint = ElementTheme.colors.iconQuaternary
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun DisabledComposerViewPreview() = ElementPreview {
    Column {
        DisabledComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
        )
    }
}
