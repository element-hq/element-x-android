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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SelectedUser(
    matrixUser: MatrixUser,
    canRemove: Boolean,
    onUserRemove: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(AvatarSize.SelectedUser.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(matrixUser.getAvatarData(size = AvatarSize.SelectedUser))
            Text(
                modifier = Modifier.clipToBounds(),
                text = matrixUser.getBestName(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Center,
            )
        }
        if (canRemove) {
            Surface(
                color = ElementTheme.colors.textPrimary,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onUserRemove(matrixUser) }
                    ),
            ) {
                Icon(
                    imageVector = CompoundIcons.Close(),
                    contentDescription = stringResource(id = CommonStrings.action_remove),
                    tint = ElementTheme.colors.iconOnSolidPrimary,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SelectedUserPreview() = ElementPreview {
    SelectedUser(
        aMatrixUser(displayName = "John Doe"),
        canRemove = true,
        onUserRemove = {},
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserCannotRemovePreview() = ElementPreview {
    SelectedUser(
        aMatrixUser(),
        canRemove = false,
        onUserRemove = {},
    )
}
