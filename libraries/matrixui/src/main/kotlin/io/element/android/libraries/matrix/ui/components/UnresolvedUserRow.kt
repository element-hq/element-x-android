/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UnresolvedUserRow(
    avatarData: AvatarData,
    id: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(avatarData)
        Column(
            modifier = Modifier
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ID
            Text(
                text = id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
            )

            // Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                Icon(
                    imageVector = CompoundIcons.ErrorSolid(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Top)
                        .padding(2.dp),
                    tint = ElementTheme.colors.iconCriticalPrimary,
                )
                Text(
                    text = stringResource(CommonStrings.common_invite_unknown_profile),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodySmRegular.copy(lineHeight = 16.sp),
                )
            }
        }
    }
}

@Preview
@Composable
internal fun UnresolvedUserRowPreview() = ElementThemedPreview {
    val matrixUser = aMatrixUser()
    UnresolvedUserRow(matrixUser.getAvatarData(size = AvatarSize.UserListItem), matrixUser.userId.value)
}
