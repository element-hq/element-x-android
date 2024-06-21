/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * https://www.figma.com/design/A2pAEvTEpJZBiOPUlcMnKi/Settings-%2B-Room-Details-(new)?node-id=1787-56333
 */

/** Ratio between the box size (120 on Figma) and the avatar size (75 on Figma). */
private const val SIZE_RATIO = 1.6f

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
