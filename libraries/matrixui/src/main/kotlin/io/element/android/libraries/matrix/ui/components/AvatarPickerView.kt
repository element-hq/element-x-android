/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

private val editIconContainerSize = 30.dp
private val editIconContainerRadius = editIconContainerSize / 2
private val editIconContainerPadding = 4.dp
private val editIconSize = 20.dp
private val editIconOffset = 8.dp

/**
 * Avatar picker view.
 *
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1949-1384
 *
 * It takes a [state], which can be [AvatarPickerState.Pick] for displaying the 'pick avatar' button, or [AvatarPickerState.Selected] when an avatar has
 * already been selected.
 *
 * Note: this function contains lots of 'magic numbers', but those are just the fractions used to scale the different dimensions based on the Figma design.
 */
@Composable
fun AvatarPickerView(
    state: AvatarPickerState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {},
    onClickLabel: String? = stringResource(CommonStrings.a11y_edit_avatar),
    enabled: Boolean = true,
) {
    val a11yAvatar = stringResource(CommonStrings.a11y_avatar)
    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = Modifier
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            onClickLabel = onClickLabel,
            onClick = onClick,
            indication = ripple(bounded = false),
        )
        .testTag(TestTags.editAvatar)
        .clearAndSetSemantics {
            contentDescription = a11yAvatar
        }

    val layoutDirection = LocalLayoutDirection.current

    fun eraseBackgroundModifier(
        parentWidth: Dp,
    ) = Modifier
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithContent {
            drawContent()
            drawCircle(
                color = Color.Black,
                center = Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr) {
                        (parentWidth - editIconContainerRadius + editIconOffset).toPx()
                    } else {
                        (editIconContainerRadius - editIconOffset).toPx()
                    },
                    y = size.height - editIconContainerRadius.toPx(),
                ),
                radius = (editIconContainerRadius + editIconContainerPadding).toPx(),
                blendMode = BlendMode.Clear,
            )
        }

    when (state) {
        is AvatarPickerState.Pick -> {
            PickButton(
                buttonSize = state.buttonSize,
                iconSize = state.iconSize,
                iconId = state.iconId,
                modifier = modifier
                    .padding(state.externalPadding)
                    .then(clickableModifier),
            )
        }
        is AvatarPickerState.Selected -> {
            Box(modifier = modifier) {
                val backgroundModifier = if (enabled) {
                    eraseBackgroundModifier(state.avatarData.size.dp)
                } else {
                    Modifier
                }
                Avatar(
                    avatarData = state.avatarData,
                    avatarType = state.type,
                    modifier = clickableModifier.then(backgroundModifier),
                )
                if (enabled) {
                    OverlayEditButton(
                        onClick = onClick,
                        interactionSource = interactionSource
                    )
                }
            }
        }
    }
}

@Composable
private fun PickButton(
    buttonSize: Dp,
    iconSize: Dp,
    @DrawableRes iconId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .border(BorderStroke(1.dp, ElementTheme.colors.borderInteractiveSecondary), shape = CircleShape)
    ) {
        Icon(
            resourceId = iconId,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize),
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@Composable
private fun BoxScope.OverlayEditButton(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(editIconContainerSize)
            .offset(x = editIconOffset)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, onClick = onClick, indication = null)
            .background(ElementTheme.colors.bgCanvasDefault)
            .border(BorderStroke(1.dp, ElementTheme.colors.borderInteractiveSecondary), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(editIconSize),
            imageVector = CompoundIcons.Edit(),
            contentDescription = null,
        )
    }
}

@Immutable
sealed interface AvatarPickerState {
    data class Pick(
        val buttonSize: Dp,
        val iconSize: Dp = buttonSize / 2,
        val externalPadding: PaddingValues = PaddingValues.Zero,
        @DrawableRes val iconId: Int = CompoundDrawables.ic_compound_take_photo,
    ) : AvatarPickerState

    data class Selected(
        val avatarData: AvatarData,
        val type: AvatarType,
    ) : AvatarPickerState
}

@PreviewsDayNight
@Composable
internal fun AvatarPickerViewPreview() = ElementPreview {
    PreviewContent()
}

@PreviewsDayNight
@Composable
internal fun AvatarPickerViewRtlPreview() = CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
) {
    ElementPreview { PreviewContent() }
}

@PreviewsDayNight
@Composable
internal fun AvatarPickerSizesPreview() = ElementPreview {
    // Size used across the codebase
    val sizes = listOf(
        AvatarSize.EditRoomDetails,
        AvatarSize.EditProfileDetails,
    )
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            sizes.forEach {
                AvatarPickerView(
                    state = AvatarPickerState.Pick(buttonSize = it.dp, externalPadding = PaddingValues(6.dp)),
                    onClick = {},
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            sizes.forEach {
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("@user:example.com", "User", "content://test", size = it),
                        type = AvatarType.User,
                    ),
                    onClick = {},
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            sizes.forEach {
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("@user:example.com", "User", "content://test", size = it),
                        type = AvatarType.Space(),
                    ),
                    onClick = {},
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviewContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val size = AvatarSize.EditRoomDetails
        Text("Pick image")
        AvatarPickerView(AvatarPickerState.Pick(buttonSize = size.dp, externalPadding = PaddingValues(6.dp)), onClick = {})
        HorizontalDivider()

        Text("User avatar")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No url")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("@user:example.com", "User", null, size = size),
                        type = AvatarType.User
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Local")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("@user:example.com", "User", "content://test", size = size),
                        type = AvatarType.User
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MXC")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("@user:example.com", "User", "mxc://test", size = size),
                        type = AvatarType.User
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        HorizontalDivider()

        Text("Room avatar")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No url")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Room", null, size = size),
                        type = AvatarType.Room()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Local")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Room", "content://test", size = size),
                        type = AvatarType.Room()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MXC")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Room", "mxc://test", size = size),
                        type = AvatarType.Room()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        HorizontalDivider()

        Text("Space avatar")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No url")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Space", null, size = size),
                        type = AvatarType.Space()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Local")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Space", "content://test", size = size),
                        type = AvatarType.Space()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MXC")
                AvatarPickerView(
                    AvatarPickerState.Selected(
                        avatarData = AvatarData("!room:example.com", "Space", "mxc://test", size = size),
                        type = AvatarType.Space()
                    ),
                    onClick = {},
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
