/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName

@Composable
fun MatrixUserRow(
    isDebugBuild: Boolean,
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.UserListItem,
    trailingContent: @Composable (() -> Unit)? = null,
) = UserRow(
    isDebugBuild = isDebugBuild,
    avatarData = matrixUser.getAvatarData(avatarSize),
    name = matrixUser.getBestName(),
    subtext = if (matrixUser.displayName.isNullOrEmpty()) null else matrixUser.userId.value,
    modifier = modifier,
    trailingContent,
)

@PreviewsDayNight
@Composable
internal fun MatrixUserRowPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) = ElementPreview {
    MatrixUserRow(isDebugBuild = false, matrixUser)
}
