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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.matrix.ui.model.getAvatarData

@Composable
fun CheckableUnresolvedUserRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    avatarData: AvatarData,
    id: String,
    modifier: Modifier = Modifier,
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
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Preview
@Composable
internal fun CheckableUnresolvedUserRowPreview() = ElementThemedPreview {
    val matrixUser = aMatrixUser()
    Column {
        CheckableUnresolvedUserRow(
            checked = false,
            onCheckedChange = { },
            avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = matrixUser.userId.value,
        )
        HorizontalDivider()
        CheckableUnresolvedUserRow(
            checked = true,
            onCheckedChange = { },
            avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = matrixUser.userId.value,
        )
        HorizontalDivider()
        CheckableUnresolvedUserRow(
            checked = false,
            onCheckedChange = { },
            avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = matrixUser.userId.value,
            enabled = false,
        )
        HorizontalDivider()
        CheckableUnresolvedUserRow(
            checked = true,
            onCheckedChange = { },
            avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = matrixUser.userId.value,
            enabled = false,
        )
    }
}
