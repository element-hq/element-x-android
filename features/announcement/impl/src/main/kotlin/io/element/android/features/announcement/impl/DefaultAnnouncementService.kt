/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.announcement.impl.fullscreen.FullscreenAnnouncementView
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class DefaultAnnouncementService(
    private val announcementStore: AnnouncementStore,
    private val announcementPresenter: AnnouncementPresenter,
) : AnnouncementService {
    override suspend fun showAnnouncement(announcement: Announcement) {
        when (announcement) {
            is Announcement.Fullscreen -> showAnnouncementIfNeverShown(announcement)
            Announcement.NewNotificationSound -> {
                announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Show)
            }
            Announcement.MultiAccount -> showAnnouncementIfNeverShown(announcement)
        }
    }

    override suspend fun onAnnouncementDismissed(announcement: Announcement) {
        announcementStore.setAnnouncementStatus(announcement, AnnouncementStatus.Shown)
    }

    override fun announcementsToShowFlow(): Flow<List<Announcement>> {
        return combine(
            flowOf(Unit),
            announcementStore.announcementStatusFlow(Announcement.NewNotificationSound),
            announcementStore.announcementStatusFlow(Announcement.MultiAccount),
        ) { _, newNotificationSoundStatus, multiAccountStatus ->
            buildList {
                if (newNotificationSoundStatus == AnnouncementStatus.Show) {
                    add(Announcement.NewNotificationSound)
                }
                // The multi-account announcement is pending until it is dismissed. Users who were already
                // logged in when the feature became available still have the default NeverShown status.
                if (multiAccountStatus != AnnouncementStatus.Shown) {
                    add(Announcement.MultiAccount)
                }
            }
        }
    }

    private suspend fun showAnnouncementIfNeverShown(announcement: Announcement) {
        val currentValue = announcementStore.announcementStatusFlow(announcement).first()
        if (currentValue == AnnouncementStatus.NeverShown) {
            announcementStore.setAnnouncementStatus(announcement, AnnouncementStatus.Show)
        }
    }

    @Composable
    override fun Render(modifier: Modifier) {
        val announcementState = announcementPresenter.present()
        FullscreenAnnouncementView(
            state = announcementState,
            modifier = modifier,
        )
    }
}
