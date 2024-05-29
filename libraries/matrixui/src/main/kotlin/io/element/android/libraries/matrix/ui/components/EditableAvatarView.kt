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

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@Composable
fun EditableAvatarView(
    matrixId: String,
    displayName: String?,
    avatarUrl: Uri?,
    avatarSize: AvatarSize,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onAvatarClick,
                    indication = rememberRipple(bounded = false),
                )
                .testTag(TestTags.editAvatar)
        ) {
            when (avatarUrl?.scheme) {
                null, "mxc" -> {
                    Avatar(
                        avatarData = AvatarData(matrixId, displayName, avatarUrl?.toString(), size = avatarSize),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    UnsavedAvatar(
                        avatarUri = avatarUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = CompoundIcons.EditSolid(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EditableAvatarViewPreview(
    @PreviewParameter(EditableAvatarViewUriProvider::class) uri: Uri?
) = ElementPreview {
    EditableAvatarView(
        matrixId = "id",
        displayName = "A room",
        avatarUrl = uri,
        avatarSize = AvatarSize.EditRoomDetails,
        onAvatarClick = {},
    )
}

open class EditableAvatarViewUriProvider : PreviewParameterProvider<Uri?> {
    override val values: Sequence<Uri?>
        get() = sequenceOf(
            null,
            Uri.parse("mxc://matrix.org/123456"),
            Uri.parse("https://example.com/avatar.jpg"),
        )
}
