/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Send button for the message composer.
 * Figma: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1956-37575&node-type=frame&m=dev
 * Temporary Figma : https://www.figma.com/design/Ni6Ii8YKtmXCKYNE90cC67/Timeline-(new)?node-id=2274-39944&m=dev
 */
@Composable
internal fun SendButton(
    canSendMessage: Boolean,
    onClick: () -> Unit,
    composerMode: MessageComposerMode,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick,
        enabled = canSendMessage,
    ) {
        val iconVector = when {
            composerMode.isEditing -> CompoundIcons.Check()
            else -> CompoundIcons.SendSolid()
        }
        val iconStartPadding = when {
            composerMode.isEditing -> 0.dp
            else -> 2.dp
        }
        val contentDescription = when {
            composerMode.isEditing -> stringResource(CommonStrings.action_edit)
            else -> stringResource(CommonStrings.action_send)
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp)
                .then(
                    if (canSendMessage) {
                        buttonBackgroundModifier()
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = iconStartPadding)
                    .align(Alignment.Center),
                imageVector = iconVector,
                contentDescription = contentDescription,
                tint = if (canSendMessage) {
                    if (ElementTheme.colors.isLight) {
                        ElementTheme.colors.iconOnSolidPrimary
                    } else {
                        ElementTheme.colors.iconPrimary
                    }
                } else {
                    ElementTheme.colors.iconQuaternary
                }
            )
        }
    }
}

private fun buttonBackgroundModifier() = Modifier.drawWithCache {
    // We have a square button, so height == width.
    val height = size.height
    val verticalGradientBrush = ShaderBrush(
        LinearGradientShader(
            from = Offset(0f, 0f),
            to = Offset(0f, height),
            colors = listOf(
                Color(0xFF79DD98),
                Color(0xFF0DBD8B),
                Color(0xFF128585),
                Color(0xFF24446B),
            )
        )
    )
    onDrawBehind {
        drawRect(
            brush = verticalGradientBrush,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SendButtonPreview() = ElementPreview {
    val normalMode = MessageComposerMode.Normal
    val editMode = MessageComposerMode.Edit(EventId("\$id").toEventOrTransactionId(), "")
    Row {
        SendButton(canSendMessage = true, onClick = {}, composerMode = normalMode)
        SendButton(canSendMessage = false, onClick = {}, composerMode = normalMode)
        SendButton(canSendMessage = true, onClick = {}, composerMode = editMode)
        SendButton(canSendMessage = false, onClick = {}, composerMode = editMode)
    }
}
