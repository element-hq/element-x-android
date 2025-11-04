/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.events.rememberEventSink
import kotlinx.coroutines.launch

@Inject
class SpaceAnnouncementPresenter(
    private val announcementStore: AnnouncementStore,
) : Presenter<SpaceAnnouncementState> {
    @Composable
    override fun present(): SpaceAnnouncementState {
        val localCoroutineScope = rememberCoroutineScope()

        val eventSink by rememberEventSink { event: SpaceAnnouncementEvents ->
            when (event) {
                SpaceAnnouncementEvents.Continue -> localCoroutineScope.launch {
                    announcementStore.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Shown)
                }
            }
        }

        return SpaceAnnouncementState(
            eventSink = eventSink,
        )
    }
}
