/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemLegacyCallInviteView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        Icon(
            imageVector = CompoundIcons.VoiceCallSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdRegular,
            text = stringResource(R.string.screen_room_timeline_legacy_call),
            textAlign = TextAlign.Start,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemLegacyCallInviteViewPreview() = ElementPreview {
    TimelineItemLegacyCallInviteView()
}
