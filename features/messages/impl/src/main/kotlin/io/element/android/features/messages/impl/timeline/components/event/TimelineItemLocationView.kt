/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.location.api.StaticMapView
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemLocationView(
    content: TimelineItemLocationContent,
    onStopLiveLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        StaticMapView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 188.dp),
            pinVariant = content.pinVariant,
            location = content.location,
            zoom = 15.0,
            contentDescription = content.description
        )

        if (content.mode is TimelineItemLocationContent.Mode.Live) {
            LiveLocationOverlay(
                mode = content.mode,
                onStopClick = onStopLiveLocationClick,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun LiveLocationOverlay(
    mode: TimelineItemLocationContent.Mode.Live,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ElementTheme.colors.bgCanvasDefault.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 1.dp,
                    color = if (mode.isActive) ElementTheme.colors.iconQuaternaryAlpha else Color.Transparent,
                    shape = iconShape,
                )
                .background(
                    color = if (mode.isActive) {
                        ElementTheme.colors.bgCanvasDefault
                    } else {
                        ElementTheme.colors.bgSubtleSecondary
                    },
                    shape = iconShape
                )
        ) {
            if (mode.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = ElementTheme.colors.iconSecondary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            } else {
                Icon(
                    imageVector = CompoundIcons.LocationPinSolid(),
                    contentDescription = null,
                    tint = if (mode.isActive) {
                        ElementTheme.colors.iconAccentPrimary
                    } else {
                        ElementTheme.colors.iconDisabled
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (mode.isActive) {
                    stringResource(CommonStrings.common_live_location)
                } else {
                    stringResource(CommonStrings.common_live_location_ended)
                },
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textPrimary,
            )
            if (mode.isActive) {
                Text(
                    text = mode.endsAt,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textPrimary,
                )
            }
        }

        if (mode.isActive && mode.canStop) {
            IconButton(
                onClick = onStopClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ElementTheme.colors.bgCriticalPrimary,
                    contentColor = ElementTheme.colors.iconOnSolidPrimary,
                )
            ) {
                Icon(
                    imageVector = CompoundIcons.Stop(),
                    contentDescription = null,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemLocationViewPreview(@PreviewParameter(TimelineItemLocationContentProvider::class) content: TimelineItemLocationContent) =
    ElementPreview {
        TimelineItemLocationView(
            content = content,
            onStopLiveLocationClick = {},
        )
    }
