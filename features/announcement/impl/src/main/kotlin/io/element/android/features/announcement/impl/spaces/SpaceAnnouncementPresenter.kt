/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch

@Inject
class SpaceAnnouncementPresenter(
    private val announcementStore: AnnouncementStore,
) : Presenter<SpaceAnnouncementState> {
    @Composable
    override fun present(): SpaceAnnouncementState {
        val localCoroutineScope = rememberCoroutineScope()

        fun handleEvent(event: SpaceAnnouncementEvents) {
            when (event) {
                SpaceAnnouncementEvents.Continue -> localCoroutineScope.launch {
                    announcementStore.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Shown)
                }
            }
        }

        return SpaceAnnouncementState(
            eventSink = ::handleEvent,
        )
    }
}
