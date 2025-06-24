/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.Collections
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val MAX_AVATAR_COUNT = 4

@Composable
internal fun AvatarCluster(
    avatars: ImmutableList<AvatarData>,
    modifier: Modifier = Modifier,
    avatarType: AvatarType = AvatarType.User,
    hideAvatarImages: Boolean = false,
    contentDescription: String? = null,
) {
    val limitedAvatars = avatars.take(MAX_AVATAR_COUNT)
    val numberOfAvatars = limitedAvatars.size
    if (numberOfAvatars == 4) {
        // Swap 2 and 3 so that the 4th avatar is at the bottom right
        Collections.swap(limitedAvatars, 2, 3)
    }
    when (numberOfAvatars) {
        0 -> {
            error("Unsupported number of avatars: 0")
        }
        1 -> {
            Avatar(
                avatarData = limitedAvatars[0],
                avatarType = avatarType,
                modifier = modifier,
                contentDescription = contentDescription,
                hideImage = hideAvatarImages
            )
        }
        else -> {
            val size = limitedAvatars.first().size
            val angle = 2 * Math.PI / numberOfAvatars
            val offsetRadius = when (numberOfAvatars) {
                2 -> size.dp.value / 4.2
                3 -> size.dp.value / 4.0
                4 -> size.dp.value / 3.1
                else -> error("Unsupported number of heroes: $numberOfAvatars")
            }
            val heroAvatarSize = when (numberOfAvatars) {
                2 -> size.dp / 2.2f
                3 -> size.dp / 2.4f
                4 -> size.dp / 2.2f
                else -> error("Unsupported number of heroes: $numberOfAvatars")
            }
            val angleOffset = when (numberOfAvatars) {
                2 -> PI
                3 -> 7 * PI / 6
                4 -> 13 * PI / 4
                else -> error("Unsupported number of heroes: $numberOfAvatars")
            }
            Box(
                modifier = modifier
                    .size(size.dp)
                    .semantics {
                        this.contentDescription = contentDescription.orEmpty()
                    },
                contentAlignment = Alignment.Center,
            ) {
                limitedAvatars.forEachIndexed { index, heroAvatar ->
                    val xOffset = (offsetRadius * cos(angle * index.toDouble() + angleOffset)).dp
                    val yOffset = (offsetRadius * sin(angle * index.toDouble() + angleOffset)).dp
                    Box(
                        modifier = Modifier
                            .size(heroAvatarSize)
                            .offset(
                                x = xOffset,
                                y = yOffset,
                            )
                    ) {
                        Avatar(
                            avatarData = heroAvatar,
                            forcedAvatarSize = heroAvatarSize,
                            avatarType = avatarType,
                            hideImage = hideAvatarImages,
                        )
                    }
                }
            }
        }
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun AvatarClusterPreview() = ElementThemedPreview {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (ngOfAvatars in 1..5) {
            AvatarCluster(
                avatars = List(ngOfAvatars) { anAvatarData(it) }.toPersistentList(),
            )
        }
    }
}

private fun anAvatarData(i: Int) = anAvatarData(
    id = ('A' + i).toString(),
    name = ('A' + i).toString()
)
