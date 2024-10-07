/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName

@Composable
fun MatrixUserHeader(
    isDebugBuild: Boolean,
    matrixUser: MatrixUser?,
    modifier: Modifier = Modifier,
    // TODO handle click on this item, to let the user be able to update their profile.
    // onClick: () -> Unit,
) {
    if (matrixUser == null) {
        MatrixUserHeaderPlaceholder(modifier = modifier)
    } else {
        MatrixUserHeaderContent(
            isDebugBuild = isDebugBuild,
            matrixUser = matrixUser,
            modifier = modifier,
            // onClick = onClick
        )
    }
}

@Composable
private fun MatrixUserHeaderContent(
    isDebugBuild: Boolean,
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    // onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            // .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            modifier = Modifier
                .padding(vertical = 12.dp),
            avatarData = matrixUser.getAvatarData(size = AvatarSize.UserPreference),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name
            Text(
                modifier = Modifier.clipToBounds(),
                text = matrixUser.getBestName(),
                maxLines = 1,
                style = ElementTheme.typography.fontHeadingSmMedium,
                overflow = TextOverflow.Ellipsis,
                color = ElementTheme.materialColors.primary,
            )
            // Id
            if (isDebugBuild && matrixUser.displayName.isNullOrEmpty().not()) { // TCHAP hide the Matrix Id in release mode
                Text(
                    text = matrixUser.userId.value,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.materialColors.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MatrixUserHeaderPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) = ElementPreview {
    MatrixUserHeader(isDebugBuild = false, matrixUser)
}
