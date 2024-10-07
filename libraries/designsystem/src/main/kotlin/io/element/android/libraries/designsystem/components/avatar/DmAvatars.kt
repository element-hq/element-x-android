/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

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
    Box(
        modifier = modifier.size(boxSize),
    ) {
        // Draw user avatar and cut top right corner
        Avatar(
            avatarData = userAvatarData,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = boxSizePx - otherAvatarRadius,
                            y = size.height - (boxSizePx - otherAvatarRadius),
                        ),
                        radius = otherAvatarRadius / 0.9f,
                        blendMode = BlendMode.Clear,
                    )
                }
                .clip(CircleShape)
                .clickable(enabled = userAvatarData.url != null) {
                    userAvatarData.url?.let { openAvatarPreview(it) }
                }
        )
        // Draw other user avatar
        Avatar(
            avatarData = otherUserAvatarData,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .clickable(enabled = otherUserAvatarData.url != null) {
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
