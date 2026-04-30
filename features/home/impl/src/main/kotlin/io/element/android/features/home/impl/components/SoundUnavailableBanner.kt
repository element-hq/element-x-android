/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.preferences.api.store.NotificationSoundUnavailableState

@Composable
internal fun SoundUnavailableBanner(
    state: NotificationSoundUnavailableState,
    onChooseSoundClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descriptionRes = when (state) {
        NotificationSoundUnavailableState.MessageSound -> R.string.screen_room_list_sound_unavailable_message_only
        NotificationSoundUnavailableState.CallRingtone -> R.string.screen_room_list_sound_unavailable_call_only
        NotificationSoundUnavailableState.Both -> R.string.screen_room_list_sound_unavailable_both
        NotificationSoundUnavailableState.None -> return
    }
    Announcement(
        // The banner appears asynchronously after a failed sound resolution. Marking it as a
        // polite live region lets TalkBack announce it without interrupting the user's flow.
        modifier = modifier
            .roomListBannerPadding()
            .semantics { liveRegion = LiveRegionMode.Polite },
        title = stringResource(R.string.screen_room_list_sound_unavailable_title),
        description = stringResource(descriptionRes),
        type = AnnouncementType.Actionable(
            actionText = stringResource(R.string.screen_room_list_sound_unavailable_action),
            onActionClick = onChooseSoundClick,
            onDismissClick = onDismissClick,
        ),
    )
}

internal class NotificationSoundUnavailableStateProvider : PreviewParameterProvider<NotificationSoundUnavailableState> {
    override val values: Sequence<NotificationSoundUnavailableState>
        get() = sequenceOf(
            NotificationSoundUnavailableState.MessageSound,
            NotificationSoundUnavailableState.CallRingtone,
            NotificationSoundUnavailableState.Both,
        )
}

@PreviewsDayNight
@Composable
internal fun SoundUnavailableBannerPreview(
    @PreviewParameter(NotificationSoundUnavailableStateProvider::class) state: NotificationSoundUnavailableState,
) = ElementPreview {
    SoundUnavailableBanner(
        state = state,
        onChooseSoundClick = {},
        onDismissClick = {},
    )
}
