/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.noFontPadding
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                text = id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                style = noFontPadding,
            )

            // Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "",
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Top)
                        .padding(2.dp),
                    tint = MaterialTheme.colorScheme.error,
                )

                Text(
                    text = stringResource(CommonStrings.common_invite_unknown_profile),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
fun CheckableUnresolvedUserRow(
    checked: Boolean,
    avatarData: AvatarData,
    id: String,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox, enabled = enabled) {
                onCheckedChange(!checked)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UnresolvedUserRow(
            modifier = Modifier.weight(1f),
            avatarData = avatarData,
            id = id,
        )

        Checkbox(
            modifier = Modifier.padding(end = 16.dp),
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@ElementPreviews
@Composable
internal fun UnresolvedUserRowPreview() {
    ElementPreview {
        val matrixUser = aMatrixUser()
        UnresolvedUserRow(matrixUser.getAvatarData(size = AvatarSize.UserListItem), matrixUser.userId.value)
    }
}

@ElementPreviews
@Composable
internal fun CheckableUnresolvedUserRowPreview() {
    ElementPreview {
        val matrixUser = aMatrixUser()
        Column {
            CheckableUnresolvedUserRow(false, matrixUser.getAvatarData(AvatarSize.UserListItem), matrixUser.userId.value)
            Divider()
            CheckableUnresolvedUserRow(true, matrixUser.getAvatarData(AvatarSize.UserListItem), matrixUser.userId.value)
            Divider()
            CheckableUnresolvedUserRow(false, matrixUser.getAvatarData(AvatarSize.UserListItem), matrixUser.userId.value, enabled = false)
            Divider()
            CheckableUnresolvedUserRow(true, matrixUser.getAvatarData(AvatarSize.UserListItem), matrixUser.userId.value, enabled = false)
        }
    }
}
