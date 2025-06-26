/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface MediaPreviewConfigStateStore {
    val hideInviteAvatars: State<Boolean>
    val timelineMediaPreviewValue: State<MediaPreviewValue>
    val setHideInviteAvatarsAction: State<AsyncAction<Unit>>
    val setTimelineMediaPreviewAction: State<AsyncAction<Unit>>

    fun setHideInviteAvatars(hide: Boolean)
    fun setTimelineMediaPreviewValue(value: MediaPreviewValue)
}

@ContributesBinding(SessionScope::class, boundType = MediaPreviewConfigStateStore::class)
@SingleIn(SessionScope::class)
class DefaultMediaPreviewConfigStateStore @Inject constructor(
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaPreviewService: MediaPreviewService,
    private val snackbarDispatcher: SnackbarDispatcher,
) : MediaPreviewConfigStateStore {
    override val hideInviteAvatars = mutableStateOf(false)
    override val timelineMediaPreviewValue = mutableStateOf(MediaPreviewValue.On)
    override val setHideInviteAvatarsAction = mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
    override val setTimelineMediaPreviewAction = mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)

    init {
        val configFlow = mediaPreviewService.getMediaPreviewConfigFlow().shareIn(sessionCoroutineScope, SharingStarted.Eagerly)
        val hideInviteAvatarsFlow = configFlow.mapNotNull { it?.hideInviteAvatar }.distinctUntilChanged()
        val timelineMediaPreviewFlow = configFlow.mapNotNull { it?.mediaPreviewValue }.distinctUntilChanged()

        hideInviteAvatarsFlow
            .onEach {
                Timber.d("Hide invi@te avatars changed to $it")
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

    override fun setHideInviteAvatars(hide: Boolean) {
        sessionCoroutineScope.launch {
            Timber.d("Setting hide invite avatars to $hide")
            val prevHideInviteAvatars = hideInviteAvatars.value
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
            Timber.d("Setting timeline media preview value to $value")
            val prevTimelineMediaPreviewValue = timelineMediaPreviewValue.value
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

