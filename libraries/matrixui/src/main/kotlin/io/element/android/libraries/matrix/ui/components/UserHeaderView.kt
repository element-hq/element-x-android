/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.CopiableTextAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UserHeaderView(
    matrixUser: MatrixUser,
    onAvatarClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            avatarData = matrixUser.getAvatarData(AvatarSize.UserHeader),
            avatarType = AvatarType.User,
            contentDescription = stringResource(CommonStrings.a11y_user_avatar),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    enabled = matrixUser.avatarUrl != null,
                    onClickLabel = stringResource(CommonStrings.action_view),
                ) {
                    onAvatarClick(matrixUser.avatarUrl!!)
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        matrixUser.displayName?.let { displayName ->
            Text(
                modifier = Modifier
                    .clipToBounds()
                    .semantics {
                        heading()
                    },
                text = displayName,
                style = ElementTheme.typography.fontHeadingMdRegular,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        CopiableTextAtom(
            text = matrixUser.userId.value,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@PreviewsDayNight
@Composable
internal fun UserHeaderViewPreview() = ElementPreview {
    UserHeaderView(
        aMatrixUser(),
        onAvatarClick = {},
    )
}
