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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName

@Composable
fun CheckableMatrixUserRow(
    checked: Boolean,
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.UserListItem,
    onCheckedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
) = CheckableUserRow(
    checked = checked,
    avatarData = matrixUser.getAvatarData(avatarSize),
    name = matrixUser.getBestName(),
    subtext = if (matrixUser.displayName.isNullOrEmpty()) null else matrixUser.userId.value,
    modifier = modifier,
    onCheckedChange = onCheckedChange,
    enabled = enabled,
)

@Composable
fun CheckableUserRow(
    checked: Boolean,
    avatarData: AvatarData,
    name: String,
    subtext: String?,
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
        UserRow(
            modifier = Modifier.weight(1f),
            avatarData = avatarData,
            name = name,
            subtext = subtext,
        )

        Checkbox(
            modifier = Modifier
                .padding(end = 16.dp),
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Preview
@Composable
internal fun CheckableMatrixUserRowLightPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@Preview
@Composable
internal fun CheckableMatrixUserRowDarkPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    Column {
        CheckableMatrixUserRow(checked = true, matrixUser)
        CheckableMatrixUserRow(checked = false, matrixUser)
        CheckableMatrixUserRow(checked = true, matrixUser, enabled = false)
        CheckableMatrixUserRow(checked = false, matrixUser, enabled = false)
    }
}
