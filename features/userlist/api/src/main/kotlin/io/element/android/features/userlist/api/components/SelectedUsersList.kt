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

package io.element.android.features.userlist.api.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.features.userlist.api.aListOfSelectedUsers
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SelectedUsersList(
    listState: LazyListState,
    selectedUsers: ImmutableList<MatrixUser>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onUserRemoved: (MatrixUser) -> Unit = {},
) {
    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(selectedUsers.toList()) { matrixUser ->
            SelectedUser(
                matrixUser = matrixUser,
                onUserRemoved = onUserRemoved,
            )
        }
    }
}

@Preview
@Composable
internal fun SelectedUsersListLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun SelectedUsersListDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    SelectedUsersList(
        listState = LazyListState(),
        selectedUsers = aListOfSelectedUsers(),
    )
}
