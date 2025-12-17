/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun MainActionButton(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val ripple = ripple(bounded = false)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                onClick = onClick,
                indication = ripple
            )
            .widthIn(min = 76.dp, max = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            contentDescription = null,
            imageVector = imageVector,
            tint = if (enabled) LocalContentColor.current else ElementTheme.colors.iconDisabled,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            title,
            style = ElementTheme.typography.fontBodyMdMedium.copy(hyphens = Hyphens.Auto),
            color = if (enabled) LocalContentColor.current else ElementTheme.colors.textDisabled,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun MainActionButtonPreview() {
    ElementThemedPreview {
        ContentsToPreview()
    }
}

@Composable
private fun ContentsToPreview() {
    Row(
        modifier = Modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MainActionButton(
            title = "Share",
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = { },
        )
        MainActionButton(
            title = "Share with a long text",
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = { },
        )
        MainActionButton(
            title = "Share",
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = { },
            enabled = false,
        )
    }
}
