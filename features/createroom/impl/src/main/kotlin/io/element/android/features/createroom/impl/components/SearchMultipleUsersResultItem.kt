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

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.matrix.ui.components.CheckableMatrixUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUnresolvedUserRow
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.usersearch.api.UserSearchResult

@Composable
fun SearchMultipleUsersResultItem(
    searchResult: UserSearchResult,
    isUserSelected: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    if (searchResult.isUnresolved) {
        CheckableUnresolvedUserRow(
            checked = isUserSelected,
            modifier = modifier,
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = searchResult.matrixUser.userId.value,
            onCheckedChange = onCheckedChange,
        )
    } else {
        CheckableMatrixUserRow(
            checked = isUserSelected,
            modifier = modifier,
            matrixUser = searchResult.matrixUser,
            avatarSize = AvatarSize.UserListItem,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview
@Composable
internal fun SearchMultipleUsersResultItemPreview() = ElementThemedPreview { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        SearchMultipleUsersResultItem(searchResult = UserSearchResult(aMatrixUser(), isUnresolved = false), isUserSelected = false)
        Divider()
        SearchMultipleUsersResultItem(searchResult = UserSearchResult(aMatrixUser(), isUnresolved = false), isUserSelected = true)
        Divider()
        SearchMultipleUsersResultItem(searchResult = UserSearchResult(aMatrixUser(), isUnresolved = true), isUserSelected = false)
        Divider()
        SearchMultipleUsersResultItem(searchResult = UserSearchResult(aMatrixUser(), isUnresolved = true), isUserSelected = true)
    }
}
