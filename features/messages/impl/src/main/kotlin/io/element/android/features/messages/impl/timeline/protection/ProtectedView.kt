/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemAspectRatioBox
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.ui.strings.CommonStrings

@SuppressWarnings("ModifierClickableOrder")
@Composable
fun ProtectedView(
    hideContent: Boolean,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (hideContent) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center,
        ) {
            ElementTheme(darkTheme = false, applySystemBarsUpdate = false) {
                // Not using a button to be able to have correct size
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(
                            onClick = onShowClick,
                            role = Role.Button,
                        )
                        .padding(4.dp)
                        .border(
                            width = 1.dp,
                            color = ElementTheme.colors.borderInteractiveSecondary,
                            shape = CircleShape,
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 4.dp,
                        ),
                    text = stringResource(CommonStrings.action_show),
                    color = ElementTheme.colors.textOnSolidPrimary,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
            }
        }
    } else {
        content()
    }
}

@PreviewsDayNight
@Composable
internal fun ProtectedViewPreview(
    @PreviewParameter(AspectRatioProvider::class) aspectRatio: Float?,
) = ElementPreview {
    TimelineItemAspectRatioBox(
        modifier = Modifier.blurHashBackground(A_BLUR_HASH, alpha = 0.9f),
        aspectRatio = coerceRatioWhenHidingContent(aspectRatio, true),
    ) {
        ProtectedView(
            hideContent = true,
            onShowClick = {},
            content = {},
        )
    }
}
