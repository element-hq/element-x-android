/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

/** Ratio between the box size (120 on Figma) and the avatar size (75 on Figma). */
private const val SIZE_RATIO = 1.6f

/**
 * https://www.figma.com/design/A2pAEvTEpJZBiOPUlcMnKi/Settings-%2B-Room-Details-(new)?node-id=1787-56333
 */
@Composable
fun DmAvatars(
    userAvatarData: AvatarData,
    otherUserAvatarData: AvatarData,
    openAvatarPreview: (url: String) -> Unit,
    openOtherAvatarPreview: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val boxSize = userAvatarData.size.dp * SIZE_RATIO
    val boxSizePx = boxSize.toPx()
    val otherAvatarRadius = otherUserAvatarData.size.dp.toPx() / 2
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        modifier = modifier.size(boxSize),
    ) {
        // Draw user avatar and cut top end corner
        Avatar(
            avatarData = userAvatarData,
            avatarType = AvatarType.User,
            contentDescription = userAvatarData.url?.let { stringResource(CommonStrings.a11y_your_avatar) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    val xOffset = if (isRtl) {
                        size.width - boxSizePx + otherAvatarRadius
                    } else {
                        boxSizePx - otherAvatarRadius
                    }
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = xOffset,
                            y = size.height - (boxSizePx - otherAvatarRadius),
                        ),
                        radius = otherAvatarRadius / 0.9f,
                        blendMode = BlendMode.Clear,
                    )
                }
                .clip(CircleShape)
                .clickable(
                    enabled = userAvatarData.url != null,
                    onClickLabel = stringResource(CommonStrings.action_view),
                ) {
                    userAvatarData.url?.let { openAvatarPreview(it) }
                }
        )
        // Draw other user avatar
        Avatar(
            avatarData = otherUserAvatarData,
            avatarType = AvatarType.User,
            contentDescription = otherUserAvatarData.url?.let { stringResource(CommonStrings.a11y_other_user_avatar) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .clickable(
                    enabled = otherUserAvatarData.url != null,
                    onClickLabel = stringResource(CommonStrings.action_view),
                ) {
                    otherUserAvatarData.url?.let { openOtherAvatarPreview(it) }
                }
                .testTag(TestTags.memberDetailAvatar)
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun DmAvatarsPreview() = ElementThemedPreview {
    val size = AvatarSize.DmCluster
    DmAvatars(
        userAvatarData = anAvatarData(
            id = "Alice",
            name = "Alice",
            size = size,
        ),
        otherUserAvatarData = anAvatarData(
            id = "Bob",
            name = "Bob",
            size = size,
        ),
        openAvatarPreview = {},
        openOtherAvatarPreview = {},
    )
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun DmAvatarsRtlPreview() {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        DmAvatarsPreview()
    }
}
