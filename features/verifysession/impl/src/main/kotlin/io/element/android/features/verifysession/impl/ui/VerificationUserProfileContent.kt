/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.UserId

@Composable
fun VerificationUserProfileContent(
    userId: UserId,
    displayName: String?,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    val avatarData = remember(userId, displayName, avatarUrl) {
        AvatarData(id = userId.value, name = displayName, url = avatarUrl, size = AvatarSize.UserVerification)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(avatarData)

        Spacer(modifier = Modifier.padding(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = displayName ?: userId.value, style = ElementTheme.typography.fontBodyLgMedium, color = ElementTheme.colors.textPrimary)

            if (displayName != null) {
                Text(text = userId.value, style = ElementTheme.typography.fontBodyMdRegular, color = ElementTheme.colors.textSecondary)
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun VerificationUserProfileContentPreview() = ElementPreview(
    drawableFallbackForImages = CommonDrawables.sample_avatar
) {
    VerificationUserProfileContent(
        userId = UserId("@alice:example.com"),
        displayName = "Alice",
        avatarUrl = "https://example.com/avatar.png",
    )
}
