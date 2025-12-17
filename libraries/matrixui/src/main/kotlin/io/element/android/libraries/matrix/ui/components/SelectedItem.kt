/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SelectedItem(
    avatarData: AvatarData,
    avatarType: AvatarType,
    text: String,
    maxLines: Int,
    a11yContentDescription: String,
    canRemove: Boolean,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionRemove = stringResource(id = CommonStrings.action_remove)
    Box(
        modifier = modifier
            .width(avatarData.size.dp)
            .clearAndSetSemantics {
                contentDescription = a11yContentDescription
                if (canRemove) {
                    // Note: this does not set the click effect to the whole Box
                    // when talkback is not enabled
                    onClick(
                        label = actionRemove,
                        action = {
                            onRemoveClick()
                            true
                        }
                    )
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val closeIconRadius = 12.dp.toPx()
            val closeIconOffset = 10.dp.toPx()
            Avatar(
                avatarData = avatarData,
                avatarType = avatarType,
                modifier = Modifier
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        if (canRemove) {
                            val xOffset = if (isRtl) {
                                closeIconOffset
                            } else {
                                size.width - closeIconOffset
                            }
                            drawCircle(
                                color = Color.Black,
                                center = Offset(
                                    x = xOffset,
                                    y = closeIconOffset,
                                ),
                                radius = closeIconRadius,
                                blendMode = BlendMode.Clear,
                            )
                        }
                    },
            )
            Text(
                modifier = Modifier.clipToBounds(),
                text = text,
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
                style = MaterialTheme.typography.bodyMedium,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (canRemove) {
            Surface(
                color = ElementTheme.colors.bgActionPrimaryRest,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clickable(
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onRemoveClick,
                    ),
            ) {
                Icon(
                    imageVector = CompoundIcons.Close(),
                    // Note: keep the context description for the test
                    contentDescription = stringResource(id = CommonStrings.action_remove),
                    tint = ElementTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}
