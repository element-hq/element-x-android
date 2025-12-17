/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.map

@Inject
class AnnouncementPresenter(
    private val announcementStore: AnnouncementStore,
) : Presenter<AnnouncementState> {
    @Composable
    override fun present(): AnnouncementState {
        val showSpaceAnnouncement by remember {
            announcementStore.announcementStatusFlow(Announcement.Space).map {
                it == AnnouncementStatus.Show
            }
        }.collectAsState(false)
        return AnnouncementState(
            showSpaceAnnouncement = showSpaceAnnouncement,
        )
    }
}
