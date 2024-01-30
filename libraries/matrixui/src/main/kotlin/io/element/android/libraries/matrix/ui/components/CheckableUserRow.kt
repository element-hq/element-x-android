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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.matrix.ui.model.getAvatarData

@Composable
fun CheckableUserRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    data: CheckableUserRowData,
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
        val rowModifier = Modifier.weight(1f)
        when (data) {
            is CheckableUserRowData.Resolved -> {
                UserRow(
                    modifier = rowModifier,
                    avatarData = data.avatarData,
                    name = data.name,
                    subtext = data.subtext,
                )
            }
            is CheckableUserRowData.Unresolved -> {
                UnresolvedUserRow(
                    modifier = rowModifier,
                    avatarData = data.avatarData,
                    id = data.id,
                )
            }
        }

        Checkbox(
            modifier = Modifier.padding(end = 8.dp),
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

sealed interface CheckableUserRowData {
    data class Resolved(
        val avatarData: AvatarData,
        val name: String,
        val subtext: String?,
    ) : CheckableUserRowData

    data class Unresolved(
        val avatarData: AvatarData,
        val id: String,
    ) : CheckableUserRowData
}

@Preview
@Composable
internal fun CheckableResolvedUserRowPreview() = ElementThemedPreview {
    val matrixUser = aMatrixUser()
    val data = CheckableUserRowData.Resolved(
        avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
        name = matrixUser.displayName.orEmpty(),
        subtext = matrixUser.userId.value,
    )
    Column {
        CheckableUserRow(
            checked = false,
            onCheckedChange = { },
            data = data,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = true,
            onCheckedChange = { },
            data = data,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = false,
            onCheckedChange = { },
            data = data,
            enabled = false,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = true,
            onCheckedChange = { },
            data = data,
            enabled = false,
        )
    }
}

@Preview
@Composable
internal fun CheckableUnresolvedUserRowPreview() = ElementThemedPreview {
    val matrixUser = aMatrixUser()
    val data = CheckableUserRowData.Unresolved(
        avatarData = matrixUser.getAvatarData(AvatarSize.UserListItem),
        id = matrixUser.userId.value,
    )
    Column {
        CheckableUserRow(
            checked = false,
            onCheckedChange = { },
            data = data,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = true,
            onCheckedChange = { },
            data = data,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = false,
            onCheckedChange = { },
            data = data,
            enabled = false,
        )
        HorizontalDivider()
        CheckableUserRow(
            checked = true,
            onCheckedChange = { },
            data = data,
            enabled = false,
        )
    }
}
