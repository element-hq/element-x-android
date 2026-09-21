/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class ModerationAndSafetyPresenter(
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val mediaPreviewConfigStateStore: MediaPreviewConfigStateStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<ModerationAndSafetyState> {
    @Composable
    override fun present(): ModerationAndSafetyState {
        val isSharePresenceEnabled by remember {
            sessionPreferencesStore.isSharePresenceEnabled()
        }.collectAsState(initial = true)

        val mediaPreviewConfigState = mediaPreviewConfigStateStore.state()

        fun handleEvent(event: ModerationAndSafetyEvent) {
            when (event) {
                is ModerationAndSafetyEvent.SetSharePresenceEnabled -> sessionCoroutineScope.launch {
                    sessionPreferencesStore.setSharePresence(event.enabled)
                }
                is ModerationAndSafetyEvent.SetHideInviteAvatars -> mediaPreviewConfigStateStore.setHideInviteAvatars(event.value)
                is ModerationAndSafetyEvent.SetTimelineMediaPreviewValue -> mediaPreviewConfigStateStore.setTimelineMediaPreviewValue(event.value)
            }
        }

        return ModerationAndSafetyState(
            isSharePresenceEnabled = isSharePresenceEnabled,
            mediaPreviewConfigState = mediaPreviewConfigState,
            eventSink = ::handleEvent,
        )
    }
}
