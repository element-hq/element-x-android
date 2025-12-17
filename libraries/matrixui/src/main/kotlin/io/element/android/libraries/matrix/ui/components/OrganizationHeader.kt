/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=3643-2048&m=dev
 */
@Composable
fun OrganizationHeader(
    avatarData: AvatarData,
    name: String,
    numberOfSpaces: Int,
    numberOfRooms: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(false),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            style = ElementTheme.typography.fontHeadingLgBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SpaceInfoRow(
            leftText = numberOfSpaces(numberOfSpaces),
            rightText = numberOfRooms(numberOfRooms),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun OrganizationHeaderPreview() = ElementPreview {
    OrganizationHeader(
        avatarData = anAvatarData(
            url = "anUrl",
            size = AvatarSize.OrganizationHeader,
        ),
        name = "Space name",
        numberOfSpaces = 9,
        numberOfRooms = 88,
    )
}
