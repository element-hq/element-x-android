/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData

@Composable
fun AvatarRow(
    avatarDataList: List<AvatarData>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        val lastItemIndex = avatarDataList.size - 1
        val avatarSize = avatarDataList.firstOrNull()?.size?.dp ?: return
        avatarDataList
            .reversed()
            .forEachIndexed { index, avatarData ->
                Avatar(
                    modifier = Modifier
                        .padding(start = avatarSize / 2 * (lastItemIndex - index))
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            // Draw content and clear the pixels for the avatar on the left.
                            drawContent()
                            if (index < lastItemIndex) {
                                drawCircle(
                                    color = Color.Black,
                                    center = Offset(
                                        x = 0f,
                                        y = size.height / 2,
                                    ),
                                    radius = avatarSize.toPx() / 2,
                                    blendMode = BlendMode.Clear,
                                )
                            }
                        }
                        .size(size = avatarSize)
                        .padding(2.dp),
                    avatarData = avatarData,
                )
            }
    }
}
