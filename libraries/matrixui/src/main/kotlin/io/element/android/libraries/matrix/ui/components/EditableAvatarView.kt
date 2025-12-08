/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
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
    avatarUrl: String?,
    avatarSize: AvatarSize,
    avatarType: AvatarType,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val a11yAvatar = stringResource(CommonStrings.a11y_avatar)
    val editIconRadius = 15.dp
    val parentHeight = avatarSize.dp
    val parentWidth = avatarSize.dp + editIconRadius / 2f
    Box(
        modifier = modifier
            .wrapContentSize()
            .size(height = parentHeight, width = parentWidth)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClickLabel = stringResource(CommonStrings.a11y_edit_avatar),
                onClick = onAvatarClick,
                indication = ripple(bounded = false),
            )
            .testTag(TestTags.editAvatar)
            .clearAndSetSemantics {
                contentDescription = a11yAvatar
            },
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = parentWidth.toPx() - editIconRadius.toPx(),
                            y = size.height - editIconRadius.toPx(),
                        ),
                        radius = (editIconRadius + 4.dp).toPx(),
                        blendMode = BlendMode.Clear,
                    )
                }
        ) {
            when {
                avatarUrl == null || avatarUrl.startsWith("mxc://") -> {
                    Avatar(
                        avatarData = AvatarData(
                            id = matrixId,
                            name = displayName,
                            url = avatarUrl,
                            size = avatarSize,
                        ),
                        avatarType = avatarType,
                    )
                }
                else -> {
                    UnsavedAvatar(
                        avatarUri = avatarUrl,
                        avatarSize = avatarSize,
                        avatarType = avatarType,
                    )
                }
            }
        }
        Icon(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(editIconRadius * 2)
                .border(1.dp, ElementTheme.colors.borderInteractiveSecondary, CircleShape)
                .padding(6.dp),
            imageVector = CompoundIcons.Edit(),
            contentDescription = null,
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EditableAvatarViewPreview(
    @PreviewParameter(EditableAvatarViewUriProvider::class) uri: String?
) = ElementPreview(
    drawableFallbackForImages = CommonDrawables.sample_avatar,
) {
    EditableAvatarView(
        matrixId = "id",
        displayName = "Room",
        avatarUrl = uri,
        avatarSize = AvatarSize.RoomDetailsHeader,
        avatarType = AvatarType.User,
        onAvatarClick = {},
    )
}

open class EditableAvatarViewUriProvider : PreviewParameterProvider<String?> {
    override val values: Sequence<String?>
        get() = sequenceOf(
            null,
            "mxc://matrix.org/123456",
            "https://example.com/avatar.jpg",
        )
}
