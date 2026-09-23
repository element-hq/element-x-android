/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.designsystem.R as DesignSystemR

@Composable
internal fun RoomAudioSection(
    isPttEnabled: Boolean,
    onPttEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(
        title = stringResource(CommonStrings.screen_room_details_room_audio),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoomAudioOption(
                title = stringResource(CommonStrings.screen_room_details_video_calls),
                selected = !isPttEnabled,
                onClick = { onPttEnabledChange(false) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = CompoundIcons.VideoCallSolid(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
            RoomAudioOption(
                title = stringResource(CommonStrings.screen_room_details_push_to_talk),
                selected = isPttEnabled,
                onClick = { onPttEnabledChange(true) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_ptt_solid),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
        }
    }
}

@Composable
private fun RoomAudioOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) ElementTheme.colors.bgSubtleSecondary else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

internal class RoomAudioSectionPreviewParam : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@PreviewsDayNight
@Composable
internal fun RoomAudioSectionPreview(@PreviewParameter(RoomAudioSectionPreviewParam::class) isPttEnabled: Boolean) {
    ElementPreview {
        RoomAudioSection(isPttEnabled = isPttEnabled, onPttEnabledChange = {})
    }
}
