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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AnnouncementPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAnnouncementPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.announcement).isNull()
        }
    }

    @Test
    fun `present - showFullscreen value depends on the value in the store`() = runTest {
        val store = InMemoryAnnouncementStore()
        val presenter = createAnnouncementPresenter(
            announcementStore = store,
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.announcement).isNull()
            store.setAnnouncementStatus(Announcement.Fullscreen.Space, AnnouncementStatus.Show)
            val updatedState = awaitItem()
            assertThat(updatedState.announcement).isEqualTo(Announcement.Fullscreen.Space)
            store.setAnnouncementStatus(Announcement.Fullscreen.Space, AnnouncementStatus.Shown)
            val finalState = awaitItem()
            assertThat(finalState.announcement).isNull()
        }
    }

    @Test
    fun `present - continue event will mark the announcement as Shown`() = runTest {
        val store = InMemoryAnnouncementStore()
        val presenter = createAnnouncementPresenter(
            announcementStore = store,
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.announcement).isNull()
            store.setAnnouncementStatus(Announcement.Fullscreen.Space, AnnouncementStatus.Show)
            val statusShow = store.announcementStatusFlow(Announcement.Fullscreen.Space).first()
            assertThat(statusShow).isEqualTo(AnnouncementStatus.Show)
            val updatedState = awaitItem()
            assertThat(updatedState.announcement).isEqualTo(Announcement.Fullscreen.Space)
            updatedState.eventSink(AnnouncementEvent.Continue(Announcement.Fullscreen.Space))
            val statusShown = store.announcementStatusFlow(Announcement.Fullscreen.Space).first()
            assertThat(statusShown).isEqualTo(AnnouncementStatus.Shown)
            val finalState = awaitItem()
            assertThat(finalState.announcement).isNull()
        }
    }
}

private fun createAnnouncementPresenter(
    announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
) = AnnouncementPresenter(
    announcementStore = announcementStore,
)
