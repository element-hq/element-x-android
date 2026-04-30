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
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAnnouncementServiceTest {
    @Test
    fun `when showing Fullscreen announcement, Fullscreen announcement is set to show only if it was never shown`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        assertThat(announcementStore.announcementStatusFlow(Announcement.Fullscreen.Space).first()).isEqualTo(AnnouncementStatus.NeverShown)
        sut.showAnnouncement(Announcement.Fullscreen.Space)
        assertThat(announcementStore.announcementStatusFlow(Announcement.Fullscreen.Space).first()).isEqualTo(AnnouncementStatus.Show)
        // Simulate user close the announcement
        sut.onAnnouncementDismissed(Announcement.Fullscreen.Space)
        // Entering again the space tab should not change the value
        sut.showAnnouncement(Announcement.Fullscreen.Space)
        assertThat(announcementStore.announcementStatusFlow(Announcement.Fullscreen.Space).first()).isEqualTo(AnnouncementStatus.Shown)
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
            announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Show)
            assertThat(awaitItem()).containsExactly(Announcement.NewNotificationSound)
            announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Shown)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `when showing SoundUnavailable announcement, status is set to Show even if previously dismissed`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        assertThat(announcementStore.announcementStatusFlow(Announcement.SoundUnavailable).first()).isEqualTo(AnnouncementStatus.NeverShown)
        sut.showAnnouncement(Announcement.SoundUnavailable)
        assertThat(announcementStore.announcementStatusFlow(Announcement.SoundUnavailable).first()).isEqualTo(AnnouncementStatus.Show)
        sut.onAnnouncementDismissed(Announcement.SoundUnavailable)
        assertThat(announcementStore.announcementStatusFlow(Announcement.SoundUnavailable).first()).isEqualTo(AnnouncementStatus.Shown)
        // A subsequent boot detecting another stale URI should re-show the banner.
        sut.showAnnouncement(Announcement.SoundUnavailable)
        assertThat(announcementStore.announcementStatusFlow(Announcement.SoundUnavailable).first()).isEqualTo(AnnouncementStatus.Show)
    }

    @Test
    fun `showAnnouncement(SoundUnavailable) is idempotent when already in Show`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(announcementStore = announcementStore)
        sut.showAnnouncement(Announcement.SoundUnavailable)
        // A second showAnnouncement before the user dismisses the first must not crash, throw, or
        // flip the state away from Show. Sanitization on subsequent boots can fire repeatedly if
        // the URI keeps failing — the service has to absorb that without surprise.
        sut.showAnnouncement(Announcement.SoundUnavailable)
        sut.showAnnouncement(Announcement.SoundUnavailable)
        assertThat(announcementStore.announcementStatusFlow(Announcement.SoundUnavailable).first()).isEqualTo(AnnouncementStatus.Show)
    }

    @Test
    fun `announcementsToShowFlow surfaces SoundUnavailable independently of NewNotificationSound`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        sut.announcementsToShowFlow().test {
            assertThat(awaitItem()).isEmpty()
            announcementStore.setAnnouncementStatus(Announcement.SoundUnavailable, AnnouncementStatus.Show)
            assertThat(awaitItem()).containsExactly(Announcement.SoundUnavailable)
            announcementStore.setAnnouncementStatus(Announcement.NewNotificationSound, AnnouncementStatus.Show)
            assertThat(awaitItem()).containsExactly(Announcement.NewNotificationSound, Announcement.SoundUnavailable)
            announcementStore.setAnnouncementStatus(Announcement.SoundUnavailable, AnnouncementStatus.Shown)
            assertThat(awaitItem()).containsExactly(Announcement.NewNotificationSound)
        }
    }

    private fun createDefaultAnnouncementService(
        announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
        announcementPresenter: AnnouncementPresenter = AnnouncementPresenter(announcementStore),
    ) = DefaultAnnouncementService(
        announcementStore = announcementStore,
        announcementPresenter = announcementPresenter,
    )
}
