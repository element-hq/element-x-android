/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

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
        val a11yAvatar = stringResource(CommonStrings.a11y_avatar)
        Box(
            modifier = Modifier
                .size(avatarSize.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onAvatarClick,
                    indication = ripple(bounded = false),
                )
                .testTag(TestTags.editAvatar)
                .clearAndSetSemantics {
                    contentDescription = a11yAvatar
                },
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
                    .background(ElementTheme.colors.iconPrimary)
                    .size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = CompoundIcons.EditSolid(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconOnSolidPrimary,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EditableAvatarViewPreview(
    @PreviewParameter(EditableAvatarViewUriProvider::class) uri: Uri?
) = ElementPreview(
    drawableFallbackForImages = CommonDrawables.sample_avatar,
) {
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
            "mxc://matrix.org/123456".toUri(),
            "https://example.com/avatar.jpg".toUri(),
        )
}
