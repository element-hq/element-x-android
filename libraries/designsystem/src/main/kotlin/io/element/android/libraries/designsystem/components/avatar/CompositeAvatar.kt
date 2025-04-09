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

@Composable
fun CompositeAvatar(
    avatarData: AvatarData,
    heroes: ImmutableList<AvatarData>,
    modifier: Modifier = Modifier,
    hideAvatarImages: Boolean = false,
    contentDescription: String? = null,
) {
    if (avatarData.url != null || heroes.isEmpty()) {
        Avatar(
            avatarData = avatarData,
            modifier = modifier,
            contentDescription = contentDescription,
            hideImage = hideAvatarImages
        )
    } else {
        val limitedHeroes = heroes.take(4)
        val numberOfHeroes = limitedHeroes.size
        if (numberOfHeroes == 4) {
            // Swap 2 and 3 so that the 4th hero is at the bottom right
            Collections.swap(limitedHeroes, 2, 3)
        }
        when (numberOfHeroes) {
            0 -> {
                error("Unsupported number of heroes: 0")
            }
            1 -> {
                Avatar(
                    avatarData = heroes[0],
                    modifier = modifier,
                    contentDescription = contentDescription,
                    hideImage = hideAvatarImages
                )
            }
            else -> {
                val angle = 2 * Math.PI / numberOfHeroes
                val offsetRadius = when (numberOfHeroes) {
                    2 -> avatarData.size.dp.value / 4.2
                    3 -> avatarData.size.dp.value / 4.0
                    4 -> avatarData.size.dp.value / 3.1
                    else -> error("Unsupported number of heroes: $numberOfHeroes")
                }
                val heroAvatarSize = when (numberOfHeroes) {
                    2 -> avatarData.size.dp / 2.2f
                    3 -> avatarData.size.dp / 2.4f
                    4 -> avatarData.size.dp / 2.2f
                    else -> error("Unsupported number of heroes: $numberOfHeroes")
                }
                val angleOffset = when (numberOfHeroes) {
                    2 -> PI
                    3 -> 7 * PI / 6
                    4 -> 13 * PI / 4
                    else -> error("Unsupported number of heroes: $numberOfHeroes")
                }
                Box(
                    modifier = modifier
                        .size(avatarData.size.dp)
                        .semantics {
                            this.contentDescription = contentDescription.orEmpty()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    limitedHeroes.forEachIndexed { index, heroAvatar ->
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
                                hideImage = hideAvatarImages,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun CompositeAvatarPreview() = ElementThemedPreview {
    val mainAvatar = anAvatarData(
        id = "Zac",
        name = "Zac",
        size = AvatarSize.RoomListItem,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(6) { nbOfHeroes ->
            CompositeAvatar(
                avatarData = mainAvatar,
                heroes = List(nbOfHeroes) { aHeroAvatarData(it) }.toPersistentList(),
            )
        }
    }
}

private fun aHeroAvatarData(i: Int) = anAvatarData(
    id = ('A' + i).toString(),
    name = ('A' + i).toString()
)
