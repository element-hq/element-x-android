/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun TimelineEncryptedHistoryBannerView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp)
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, ElementTheme.colors.borderInfoSubtle, MaterialTheme.shapes.small)
            .background(ElementTheme.colors.bgInfoSubtle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = CompoundIcons.InfoSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.iconInfoPrimary
        )
        Text(
            text = stringResource(R.string.screen_room_encrypted_history_banner),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textInfoPrimary
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EncryptedHistoryBannerViewPreview() = ElementPreview {
    TimelineEncryptedHistoryBannerView()
}
