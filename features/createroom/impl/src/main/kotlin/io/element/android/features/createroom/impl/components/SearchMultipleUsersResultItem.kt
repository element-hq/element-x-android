/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRowData
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.usersearch.api.UserSearchResult

@Composable
fun SearchMultipleUsersResultItem(
    isDebugBuild: Boolean,
    searchResult: UserSearchResult,
    isUserSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = if (searchResult.isUnresolved) {
        CheckableUserRowData.Unresolved(
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = searchResult.matrixUser.userId.value,
        )
    } else {
        CheckableUserRowData.Resolved(
            name = searchResult.matrixUser.getBestName(),
            subtext = if (searchResult.matrixUser.displayName.isNullOrEmpty()) null else searchResult.matrixUser.userId.value,
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
        )
    }
    CheckableUserRow(
        isDebugBuild = isDebugBuild,
        checked = isUserSelected,
        modifier = modifier,
        data = data,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Composable
internal fun SearchMultipleUsersResultItemPreview() = ElementThemedPreview {
    Column {
        SearchMultipleUsersResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
    }
}
