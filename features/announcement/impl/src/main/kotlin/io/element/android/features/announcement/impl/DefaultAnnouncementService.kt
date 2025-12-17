/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementState
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementView
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@ContributesBinding(AppScope::class)
class DefaultAnnouncementService(
    private val announcementStore: AnnouncementStore,
    private val announcementPresenter: Presenter<AnnouncementState>,
    private val spaceAnnouncementPresenter: Presenter<SpaceAnnouncementState>,
) : AnnouncementService {
    override suspend fun showAnnouncement(announcement: Announcement) {
        when (announcement) {
            Announcement.Space -> showSpaceAnnouncement()
            Announcement.NewNotificationSound -> {
                announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Show)
            }
        }
    }

    override suspend fun onAnnouncementDismissed(announcement: Announcement) {
        announcementStore.setAnnouncementStatus(announcement, AnnouncementStatus.Shown)
    }

    override fun announcementsToShowFlow(): Flow<List<Announcement>> {
        return combine(
            announcementStore.announcementStatusFlow(Announcement.Space),
            announcementStore.announcementStatusFlow(Announcement.NewNotificationSound),
        ) { spaceAnnouncementStatus, newNotificationSoundStatus ->
            buildList {
                if (spaceAnnouncementStatus == AnnouncementStatus.Show) {
                    add(Announcement.Space)
                }
                if (newNotificationSoundStatus == AnnouncementStatus.Show) {
                    add(Announcement.NewNotificationSound)
                }
            }
        }
    }

    private suspend fun showSpaceAnnouncement() {
        val currentValue = announcementStore.announcementStatusFlow(Announcement.Space).first()
        if (currentValue == AnnouncementStatus.NeverShown) {
            announcementStore.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Show)
        }
    }

    @Composable
    override fun Render(modifier: Modifier) {
        val announcementState = announcementPresenter.present()
        Box(modifier = modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = announcementState.showSpaceAnnouncement,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val spaceAnnouncementState = spaceAnnouncementPresenter.present()
                SpaceAnnouncementView(
                    state = spaceAnnouncementState,
                )
            }
        }
    }
}
