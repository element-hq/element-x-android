/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.live.ActiveLiveLocationShare
import io.element.android.features.location.test.FakeActiveLiveLocationShareManager
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.aLocation
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemLocationContent
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Clock

@RunWith(AndroidJUnit4::class)
class TimelineItemLocationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `own live location can be stopped when the room has an active share`() = runTest {
        val liveLocationShareManager = FakeActiveLiveLocationShareManager()
        val presenter = TimelineItemLocationPresenter(
            room = FakeJoinedRoom(),
            liveLocationShareManager = liveLocationShareManager,
            content = aTimelineItemLocationContent(
                senderId = A_SESSION_ID,
                mode = TimelineItemLocationContent.Mode.Live(
                    lastKnownLocation = aLocation(),
                    isActive = true,
                    endsAt = "Ends at 12:34",
                    endTimestamp = System.currentTimeMillis() + 60_000,
                ),
            ),
        )

        testPresenter(presenter) {
            val initial = awaitItem()
            assertThat((initial.mode as TimelineItemLocationContent.Mode.Live).canStop).isFalse()

            liveLocationShareManager.givenActiveShare(
                ActiveLiveLocationShare(
                    roomId = A_ROOM_ID,
                    expiresAt = Clock.System.now(),
                )
            )

            val updated = awaitItem()
            assertThat((updated.mode as TimelineItemLocationContent.Mode.Live).canStop).isTrue()
        }
    }

    private suspend fun TestScope.testPresenter(
        presenter: TimelineItemLocationPresenter,
        testBlock: suspend TurbineTestContext<TimelineItemLocationContent>.() -> Unit,
    ) {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            testBlock()
        }
    }
}
