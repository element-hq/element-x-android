/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue

class FakeMediaPreviewConfigStateStore(
    hideInviteAvatarsValue: Boolean = false,
    timelineMediaPreviewValue: MediaPreviewValue = MediaPreviewValue.On,
    setHideInviteAvatarsActionValue: AsyncAction<Unit> = AsyncAction.Uninitialized,
    setTimelineMediaPreviewActionValue: AsyncAction<Unit> = AsyncAction.Uninitialized,
) : MediaPreviewConfigStateStore {
    private val hideInviteAvatars = mutableStateOf(hideInviteAvatarsValue)
    private val timelineMediaPreviewValue = mutableStateOf(timelineMediaPreviewValue)
    private val setHideInviteAvatarsAction = mutableStateOf(setHideInviteAvatarsActionValue)
    private val setTimelineMediaPreviewAction = mutableStateOf(setTimelineMediaPreviewActionValue)

    private val setHideInviteAvatarsEvents = mutableListOf<Boolean>()
    private val setTimelineMediaPreviewValueEvents = mutableListOf<MediaPreviewValue>()

    @Composable
    override fun state(): MediaPreviewConfigState {
        return MediaPreviewConfigState(
            hideInviteAvatars = hideInviteAvatars.value,
            timelineMediaPreviewValue = timelineMediaPreviewValue.value,
            setHideInviteAvatarsAction = setHideInviteAvatarsAction.value,
            setTimelineMediaPreviewAction = setTimelineMediaPreviewAction.value,
        )
    }

    override fun setHideInviteAvatars(hide: Boolean) {
        setHideInviteAvatarsEvents.add(hide)
        hideInviteAvatars.value = hide
    }

    override fun setTimelineMediaPreviewValue(value: MediaPreviewValue) {
        setTimelineMediaPreviewValueEvents.add(value)
        timelineMediaPreviewValue.value = value
    }

    fun getSetHideInviteAvatarsEvents(): List<Boolean> = setHideInviteAvatarsEvents.toList()
    fun getSetTimelineMediaPreviewValueEvents(): List<MediaPreviewValue> = setTimelineMediaPreviewValueEvents.toList()
}
