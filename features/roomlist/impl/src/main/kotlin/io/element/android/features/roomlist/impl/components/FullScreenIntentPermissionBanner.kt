/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun FullScreenIntentPermissionBanner(
    state: FullScreenIntentPermissionsState,
    modifier: Modifier = Modifier
) {
    Announcement(
        title = stringResource(R.string.full_screen_intent_banner_title),
        description = stringResource(R.string.full_screen_intent_banner_message),
        type = AnnouncementType.Actionable(
            actionText = stringResource(CommonStrings.action_continue),
            onDismissClick = state.dismissFullScreenIntentBanner,
            onActionClick = state.openFullScreenIntentSettings,
        ),
        modifier = modifier.roomListBannerPadding(),
    )
}

@PreviewsDayNight
@Composable
internal fun FullScreenIntentPermissionBannerPreview() {
    ElementPreview {
        FullScreenIntentPermissionBanner(aFullScreenIntentPermissionsState())
    }
}
