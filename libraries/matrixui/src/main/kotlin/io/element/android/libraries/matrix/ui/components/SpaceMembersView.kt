/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.molecules.MembersCountMolecule
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarRow
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=3729-605&m=dev
 */
@Composable
fun SpaceMembersView(
    heroes: ImmutableList<MatrixUser>,
    numberOfMembers: Int,
    modifier: Modifier = Modifier,
) {
    if (heroes.isEmpty()) {
        MembersCountMolecule(
            memberCount = numberOfMembers,
            modifier = modifier,
        )
    } else {
        SpaceMembersWithAvatar(
            heroes = heroes
                .take(3)
                .map {
                    it.getAvatarData(AvatarSize.SpaceMember)
                }
                .toImmutableList(),
            numberOfMembers = numberOfMembers,
            modifier = modifier,
        )
    }
}

@Composable
private fun SpaceMembersWithAvatar(
    heroes: ImmutableList<AvatarData>,
    numberOfMembers: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AvatarRow(
            avatarDataList = heroes,
            avatarType = AvatarType.User,
            lastOnTop = true,
        )
        Text(
            text = "$numberOfMembers",
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
@PreviewsDayNight
internal fun SpaceMembersViewNoHeroesPreview() = ElementPreview {
    SpaceMembersView(
        heroes = persistentListOf(),
        numberOfMembers = 123,
    )
}

@Composable
@PreviewsDayNight
internal fun SpaceMembersViewPreview() = ElementPreview(
    drawableFallbackForImages = CommonDrawables.sample_avatar,
) {
    SpaceMembersView(
        heroes = persistentListOf(
            aMatrixUser(id = "@1:d", displayName = "Alice", avatarUrl = "aUrl"),
            aMatrixUser(id = "@2:d", displayName = "Bob"),
            aMatrixUser(id = "@3:d", displayName = "Charlie", avatarUrl = "aUrl"),
            aMatrixUser(id = "@4:d", displayName = "Dave"),
        ),
        numberOfMembers = 123,
    )
}
