/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * A list item with an Avatar as leading content.
 *
 * Figma link : https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1979-1894&m=dev
 *
 * @param avatarData The data for the avatar.
 * @param avatarType The type of avatar to display.
 * @param headline The main text of the list item.
 * @param modifier The modifier to apply to the list item.
 * @param supportingText The supporting text displayed below the headline.
 * @param trailingContent The trailing content of the list item.
 * @param enabled Whether the list item is enabled.
 * @param style The style of the list item.
 * @param onClick The callback to invoke when the list item is clicked.
 */
@Composable
fun AvatarListItem(
    avatarData: AvatarData,
    avatarType: AvatarType,
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailingContent: ListItemContent? = null,
    enabled: Boolean = true,
    style: ListItemStyle = ListItemStyle.Default,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(headline) },
        supportingContent = supportingText?.let { @Composable { Text(it) } },
        leadingContent = ListItemContent.Custom { _ ->
            Avatar(
                avatarData = avatarData,
                avatarType = avatarType,
            )
        },
        trailingContent = trailingContent,
        style = style,
        enabled = enabled,
        onClick = onClick,
    )
}
