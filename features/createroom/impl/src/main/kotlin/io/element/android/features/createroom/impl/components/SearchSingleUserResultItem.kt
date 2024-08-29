/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.matrix.ui.components.UnresolvedUserRow
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.usersearch.api.UserSearchResult

@Composable
fun SearchSingleUserResultItem(
    isDebugBuild: Boolean,
    searchResult: UserSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (searchResult.isUnresolved) {
        UnresolvedUserRow(
            modifier = modifier.clickable(onClick = onClick),
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = searchResult.matrixUser.userId.value,
        )
    } else {
        MatrixUserRow(
            isDebugBuild = isDebugBuild,
            modifier = modifier.clickable(onClick = onClick),
            matrixUser = searchResult.matrixUser,
            avatarSize = AvatarSize.UserListItem,
        )
    }
}

@Preview
@Composable
internal fun SearchSingleUserResultItemPreview() = ElementThemedPreview {
    Column {
        SearchSingleUserResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(aMatrixUser(), isUnresolved = false),
            onClick = {},
        )
        HorizontalDivider()
        SearchSingleUserResultItem(
            isDebugBuild = false,
            searchResult = UserSearchResult(aMatrixUser(), isUnresolved = true),
            onClick = {},
        )
    }
}
