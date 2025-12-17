/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AnnouncementPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAnnouncementPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.showSpaceAnnouncement).isFalse()
        }
    }

    @Test
    fun `present - showSpaceAnnouncement value depends on the value in the store`() = runTest {
        val store = InMemoryAnnouncementStore()
        val presenter = createAnnouncementPresenter(
            announcementStore = store,
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.showSpaceAnnouncement).isFalse()
            store.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Show)
            val updatedState = awaitItem()
            assertThat(updatedState.showSpaceAnnouncement).isTrue()
            store.setAnnouncementStatus(Announcement.Space, AnnouncementStatus.Shown)
            val finalState = awaitItem()
            assertThat(finalState.showSpaceAnnouncement).isFalse()
        }
    }
}

private fun createAnnouncementPresenter(
    announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
) = AnnouncementPresenter(
    announcementStore = announcementStore,
)
