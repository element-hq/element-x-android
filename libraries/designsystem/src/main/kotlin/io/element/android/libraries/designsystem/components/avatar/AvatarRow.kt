/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.internal.OverlapRatioProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Draw a row of avatars (they must all have the same size), from start to end.
 * @param avatarDataList the avatars to render. Note: they will all be rendered, the caller may
 * want to limit the list size
 * @param avatarType the type of avatars to render
 * @param modifier Jetpack Compose modifier
 * @param overlapRatio the overlap ration. When 0f, avatars will render without overlap, when 1f
 * only the first avatar will be visible
 * @param lastOnTop if true, the last visible avatar will be rendered on top.
 */
@Composable
fun AvatarRow(
    avatarDataList: ImmutableList<AvatarData>,
    avatarType: AvatarType,
    modifier: Modifier = Modifier,
    overlapRatio: Float = 0.5f,
    lastOnTop: Boolean = false,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        modifier = modifier,
    ) {
        val lastItemIndex = avatarDataList.size - 1
        val avatarSize = avatarDataList.firstOrNull()?.size?.dp ?: return
        val avatarSizePx = avatarSize.toPx()
        avatarDataList
            .let {
                if (lastOnTop) {
                    it
                } else {
                    it.reversed()
                }
            }
            .forEachIndexed { index, avatarData ->
                val startPadding = if (lastOnTop) {
                    avatarSize * (1 - overlapRatio) * index
                } else {
                    avatarSize * (1 - overlapRatio) * (lastItemIndex - index)
                }
                Avatar(
                    modifier = Modifier
                        .padding(start = startPadding)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            // Draw content and clear the pixels for the avatar on the left (right in RTL) or when lastOnTop is true on
                            // the right (left in RTL).
                            drawContent()
                            if (index < lastItemIndex) {
                                val xOffset = if (isRtl == lastOnTop) {
                                    avatarSizePx * (overlapRatio - 0.5f)
                                } else {
                                    size.width - avatarSizePx * (overlapRatio - 0.5f)
                                }
                                drawCircle(
                                    color = Color.Black,
                                    center = Offset(
                                        x = xOffset,
                                        y = size.height / 2,
                                    ),
                                    radius = avatarSizePx / 2,
                                    blendMode = BlendMode.Clear,
                                )
                            }
                        }
                        .size(size = avatarSize)
                        // Keep internal padding, it has the advantage to not reduce the size of the Avatar image,
                        // which is already small in our use case.
                        .padding(2.dp),
                    avatarData = avatarData,
                    avatarType = avatarType,
                )
            }
    }
}

@Composable
@PreviewsDayNight
internal fun AvatarRowPreview(@PreviewParameter(OverlapRatioProvider::class) overlapRatio: Float) {
    ElementPreview {
        ContentToPreview(overlapRatio)
    }
}

@Composable
@PreviewsDayNight
internal fun AvatarRowLastOnTopPreview(@PreviewParameter(OverlapRatioProvider::class) overlapRatio: Float) {
    ElementPreview {
        ContentToPreview(
            overlapRatio = overlapRatio,
            lastOnTop = true,
        )
    }
}

@Composable
@PreviewsDayNight
internal fun AvatarRowRtlPreview(@PreviewParameter(OverlapRatioProvider::class) overlapRatio: Float) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        ElementPreview {
            ContentToPreview(overlapRatio)
        }
    }
}

@Composable
@PreviewsDayNight
internal fun AvatarRowLastOnTopRtlPreview(@PreviewParameter(OverlapRatioProvider::class) overlapRatio: Float) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        ElementPreview {
            ContentToPreview(
                overlapRatio = overlapRatio,
                lastOnTop = true,
            )
        }
    }
}

@Composable
private fun ContentToPreview(
    overlapRatio: Float,
    lastOnTop: Boolean = false,
) {
    AvatarRow(
        avatarDataList = listOf("A", "B", "C").map {
            AvatarData(
                id = it,
                name = it,
                size = AvatarSize.RoomListItem,
            )
        }.toImmutableList(),
        avatarType = AvatarType.User,
        overlapRatio = overlapRatio,
        lastOnTop = lastOnTop,
    )
}
