/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.store.AnnouncementStatus
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpaceAnnouncementPresenterTest {
    @Test
    fun `present - when user continues, the store is updated`() = runTest {
        val store = InMemoryAnnouncementStore()
        val presenter = createSpaceAnnouncementPresenter(
            announcementStore = store,
        )
        presenter.test {
            assertThat(store.announcementStatusFlow(Announcement.Space).first()).isEqualTo(AnnouncementStatus.NeverShown)
            val state = awaitItem()
            state.eventSink(SpaceAnnouncementEvents.Continue)
            assertThat(store.announcementStatusFlow(Announcement.Space).first()).isEqualTo(AnnouncementStatus.Shown)
        }
    }
}

private fun createSpaceAnnouncementPresenter(
    announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
) = SpaceAnnouncementPresenter(
    announcementStore = announcementStore,
)
