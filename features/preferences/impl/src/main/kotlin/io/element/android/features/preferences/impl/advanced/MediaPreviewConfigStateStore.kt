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
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

data class MediaPreviewConfigState(
    val hideInviteAvatars: Boolean,
    val timelineMediaPreviewValue: MediaPreviewValue,
    val setHideInviteAvatarsAction: AsyncAction<Unit>,
    val setTimelineMediaPreviewAction: AsyncAction<Unit>,
)

interface MediaPreviewConfigStateStore {
    @Composable
    fun state(): MediaPreviewConfigState
    fun setHideInviteAvatars(hide: Boolean)
    fun setTimelineMediaPreviewValue(value: MediaPreviewValue)
}

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultMediaPreviewConfigStateStore(
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaPreviewService: MediaPreviewService,
    private val snackbarDispatcher: SnackbarDispatcher,
) : MediaPreviewConfigStateStore {
    private val hideInviteAvatars = mutableStateOf(false)
    private val timelineMediaPreviewValue = mutableStateOf(MediaPreviewValue.On)
    private val setHideInviteAvatarsAction = mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
    private val setTimelineMediaPreviewAction = mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)

    init {
        val configFlow = mediaPreviewService.mediaPreviewConfigFlow
        val hideInviteAvatarsFlow = configFlow.map { it.hideInviteAvatar }.distinctUntilChanged()
        val timelineMediaPreviewFlow = configFlow.map { it.mediaPreviewValue }.distinctUntilChanged()

        hideInviteAvatarsFlow
            .onEach {
                Timber.d("Hide invite avatars changed to $it")
                hideInviteAvatars.value = it
            }
            .launchIn(sessionCoroutineScope)

        timelineMediaPreviewFlow
            .onEach {
                Timber.d("Timeline media preview value changed to $it")
                timelineMediaPreviewValue.value = it
            }
            .launchIn(sessionCoroutineScope)
    }

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
        sessionCoroutineScope.launch {
            val prevHideInviteAvatars = hideInviteAvatars.value
            if (prevHideInviteAvatars == hide) return@launch
            Timber.d("Setting hide invite avatars to $hide")
            hideInviteAvatars.value = hide
            runUpdatingState(setHideInviteAvatarsAction) {
                mediaPreviewService
                    .setHideInviteAvatars(hide)
                    .onFailure {
                        hideInviteAvatars.value = prevHideInviteAvatars
                        snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_something_went_wrong_message))
                    }
            }
        }
    }

    override fun setTimelineMediaPreviewValue(value: MediaPreviewValue) {
        sessionCoroutineScope.launch {
            val prevTimelineMediaPreviewValue = timelineMediaPreviewValue.value
            if (prevTimelineMediaPreviewValue == value) return@launch
            Timber.d("Setting timeline media preview value to $value")
            timelineMediaPreviewValue.value = value
            runUpdatingState(setTimelineMediaPreviewAction) {
                mediaPreviewService
                    .setMediaPreviewValue(value)
                    .onFailure {
                        timelineMediaPreviewValue.value = prevTimelineMediaPreviewValue
                        snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_something_went_wrong_message))
                    }
            }
        }
    }
}
