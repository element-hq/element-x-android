/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

// NOTE: strings + banner colours are hard-coded (inverted dark banner, both themes) — final values
// come from Compound tokens / Figma once the prototype graduates.
private val bannerBackgroundColor = Color(0xFF1B1B1D)
private val bannerTitleColor = Color.White
private val bannerSubtitleColor = Color(0xB3FFFFFF)

/**
 * In-room banner shown when a PTT session is live and the local user has NOT joined it.
 * Offers a one-tap Join. Audio-only.
 */
@Composable
internal fun PttSessionBanner(
    participantCount: Int,
    avatarData: AvatarData,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bannerBackgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Room(),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Push to talk session",
                style = ElementTheme.typography.fontBodyMdMedium,
                color = bannerTitleColor,
            )
            Text(
                text = "$participantCount current participants",
                style = ElementTheme.typography.fontBodySmRegular,
                color = bannerSubtitleColor,
            )
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onJoinClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.heightIn(min = 36.dp),
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_ptt),
                contentDescription = null,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Join",
                style = ElementTheme.typography.fontBodyMdMedium,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PttSessionBannerPreview(
    @PreviewParameter(PttSessionBannerParticipantsProvider::class) participantCount: Int
) = ElementPreview {
    PttSessionBanner(
        participantCount = participantCount,
        avatarData = anAvatarData(),
        onJoinClick = {},
    )
}

internal class PttSessionBannerParticipantsProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int> = sequenceOf(1, 9)
}
