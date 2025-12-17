/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName

@Composable
fun SelectedUser(
    matrixUser: MatrixUser,
    canRemove: Boolean,
    onUserRemove: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectedItem(
        avatarData = matrixUser.getAvatarData(size = AvatarSize.SelectedUser),
        avatarType = AvatarType.User,
        text = matrixUser.getBestName(),
        maxLines = 2,
        a11yContentDescription = matrixUser.getBestName(),
        canRemove = canRemove,
        onRemoveClick = { onUserRemove(matrixUser) },
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserPreview(@PreviewParameter(MatrixUserWithAvatarProvider::class) user: MatrixUser) = ElementPreview {
    SelectedUser(
        matrixUser = user,
        canRemove = true,
        onUserRemove = {},
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserRtlPreview() = CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
) {
    ElementPreview {
        SelectedUser(
            matrixUser = aMatrixUser(displayName = "John Doe"),
            canRemove = true,
            onUserRemove = {},
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SelectedUserCannotRemovePreview() = ElementPreview {
    SelectedUser(
        matrixUser = aMatrixUser(),
        canRemove = false,
        onUserRemove = {},
    )
}
