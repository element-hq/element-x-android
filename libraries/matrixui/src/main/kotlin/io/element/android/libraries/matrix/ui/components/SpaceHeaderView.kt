/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewDescriptionAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewTitleAtom
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=3643-2429&m=dev
 */
@Composable
fun SpaceHeaderView(
    avatarData: AvatarData,
    name: String?,
    topic: String?,
    joinRule: JoinRule?,
    heroes: ImmutableList<MatrixUser>,
    numberOfMembers: Int,
    numberOfRooms: Int,
    modifier: Modifier = Modifier,
    topicMaxLines: Int = Int.MAX_VALUE,
) {
    RoomPreviewOrganism(
        modifier = modifier.padding(24.dp),
        avatar = {
            Avatar(
                avatarData = avatarData,
                avatarType = AvatarType.Space(),
            )
        },
        title = {
            name?.let {
                RoomPreviewTitleAtom(title = name)
            }
        },
        subtitle = {
            if (joinRule != null) {
                SpaceInfoRow(
                    joinRule = joinRule,
                    numberOfRooms = numberOfRooms,
                )
            }
        },
        description = if (topic != null) {
            { RoomPreviewDescriptionAtom(description = topic, maxLines = topicMaxLines) }
        } else {
            null
        },
        memberCount = {
            SpaceMembersView(
                heroes = heroes,
                numberOfMembers = numberOfMembers,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        },
    )
}

@PreviewsDayNight
@Composable
internal fun SpaceHeaderViewPreview() = ElementPreview {
    SpaceHeaderView(
        avatarData = anAvatarData(
            url = "anUrl",
            size = AvatarSize.SpaceHeader,
        ),
        name = "Space name",
        topic = "Space topic: " + LoremIpsum(40).values.first(),
        topicMaxLines = 2,
        joinRule = JoinRule.Public,
        heroes = persistentListOf(
            aMatrixUser(id = "@1:d", displayName = "Alice", avatarUrl = "aUrl"),
            aMatrixUser(id = "@2:d", displayName = "Bob"),
            aMatrixUser(id = "@3:d", displayName = "Charlie", avatarUrl = "aUrl"),
            aMatrixUser(id = "@4:d", displayName = "Dave"),
        ),
        numberOfMembers = 999,
        numberOfRooms = 10,
    )
}
