/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementState
import io.element.android.features.announcement.impl.spaces.aSpaceAnnouncementState
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAnnouncementServiceTest {
    @Test
    fun `when showing Space announcement, space announcement is set to show only if it was never shown`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        assertThat(announcementStore.announcementStatusFlow(Announcement.Space).first()).isEqualTo(AnnouncementStatus.NeverShown)
        sut.showAnnouncement(Announcement.Space)
        assertThat(announcementStore.announcementStatusFlow(Announcement.Space).first()).isEqualTo(AnnouncementStatus.Show)
        // Simulate user close the announcement
        sut.onAnnouncementDismissed(Announcement.Space)
        // Entering again the space tab should not change the value
        sut.showAnnouncement(Announcement.Space)
        assertThat(announcementStore.announcementStatusFlow(Announcement.Space).first()).isEqualTo(AnnouncementStatus.Shown)
    }

    @Test
    fun `when showing NewNotificationSound announcement, announcement is set to show even if it was already shown`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        assertThat(announcementStore.announcementStatusFlow(Announcement.NewNotificationSound).first()).isEqualTo(AnnouncementStatus.NeverShown)
        sut.showAnnouncement(Announcement.NewNotificationSound)
        assertThat(announcementStore.announcementStatusFlow(Announcement.NewNotificationSound).first()).isEqualTo(AnnouncementStatus.Show)
        // Simulate user close the announcement
        sut.onAnnouncementDismissed(Announcement.NewNotificationSound)
        // Calling again showAnnouncement should set it back to Show
        sut.showAnnouncement(Announcement.NewNotificationSound)
        assertThat(announcementStore.announcementStatusFlow(Announcement.NewNotificationSound).first()).isEqualTo(AnnouncementStatus.Show)
    }

    @Test
    fun `test announcementsToShowFlow`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        sut.announcementsToShowFlow().test {
            assertThat(awaitItem()).isEmpty()
            announcementStore.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Show)
            assertThat(awaitItem()).containsExactly(Announcement.Space)
            announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Show)
            assertThat(awaitItem()).containsExactly(Announcement.Space, Announcement.NewNotificationSound)
            announcementStore.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Shown)
            assertThat(awaitItem()).containsExactly(Announcement.NewNotificationSound)
            announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Shown)
            assertThat(awaitItem()).isEmpty()
        }
    }

    private fun createDefaultAnnouncementService(
        announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
        announcementPresenter: Presenter<AnnouncementState> = Presenter { anAnnouncementState() },
        spaceAnnouncementPresenter: Presenter<SpaceAnnouncementState> = Presenter { aSpaceAnnouncementState() },
    ) = DefaultAnnouncementService(
        announcementStore = announcementStore,
        announcementPresenter = announcementPresenter,
        spaceAnnouncementPresenter = spaceAnnouncementPresenter,
    )
}
