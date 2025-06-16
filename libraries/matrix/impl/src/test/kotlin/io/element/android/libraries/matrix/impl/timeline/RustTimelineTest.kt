/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.matrix.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimeline
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineDiff
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.TimelineChange
import uniffi.matrix_sdk.RoomPaginationStatus
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

class RustTimelineTest {
    @Test
    fun `ensure that the timeline emits new loading item when pagination does not bring new events`() = runTest {
        val inner = FakeFfiTimeline()
        val systemClock = FakeSystemClock()
        val sut = createRustTimeline(
            inner = inner,
            systemClock = systemClock,
        )
        sut.timelineItems.test {
            // Give time for the listener to be set
            runCurrent()
            inner.emitDiff(
                listOf(
                    FakeFfiTimelineDiff(
                        item = null,
                        change = TimelineChange.RESET,
                    )
                )
            )
            with(awaitItem()) {
                assertThat(size).isEqualTo(1)
                // Typing notification
                assertThat((get(0) as MatrixTimelineItem.Virtual).virtual).isEqualTo(VirtualTimelineItem.TypingNotification)
            }
            with(awaitItem()) {
                assertThat(size).isEqualTo(2)
                // The loading
                assertThat((get(0) as MatrixTimelineItem.Virtual).virtual).isEqualTo(
                    VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.BACKWARDS,
                        timestamp = A_FAKE_TIMESTAMP,
                    )
                )
                // Typing notification
                assertThat((get(1) as MatrixTimelineItem.Virtual).virtual).isEqualTo(VirtualTimelineItem.TypingNotification)
            }
            systemClock.epochMillisResult = A_FAKE_TIMESTAMP + 1
            // Start pagination
            sut.paginate(Timeline.PaginationDirection.BACKWARDS)
            // Simulate SDK starting pagination
            inner.emitPaginationStatus(RoomPaginationStatus.Paginating)
            // No new events received
            // Simulate SDK stopping pagination, more event to load
            inner.emitPaginationStatus(RoomPaginationStatus.Idle(hitTimelineStart = false))
            // expect an item to be emitted, with an updated timestamp
            with(awaitItem()) {
                assertThat(size).isEqualTo(2)
                // The loading
                assertThat((get(0) as MatrixTimelineItem.Virtual).virtual).isEqualTo(
                    VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.BACKWARDS,
                        timestamp = A_FAKE_TIMESTAMP + 1,
                    )
                )
                // Typing notification
                assertThat((get(1) as MatrixTimelineItem.Virtual).virtual).isEqualTo(VirtualTimelineItem.TypingNotification)
            }
        }
    }
}

private fun TestScope.createRustTimeline(
    inner: InnerTimeline,
    mode: Timeline.Mode = Timeline.Mode.LIVE,
    systemClock: SystemClock = FakeSystemClock(),
    joinedRoom: JoinedRoom = FakeJoinedRoom().apply { givenRoomInfo(aRoomInfo()) },
    coroutineScope: CoroutineScope = backgroundScope,
    dispatcher: CoroutineDispatcher = testCoroutineDispatchers().io,
    roomContentForwarder: RoomContentForwarder = RoomContentForwarder(FakeFfiRoomListService()),
    featureFlagsService: FeatureFlagService = FakeFeatureFlagService(),
    onNewSyncedEvent: () -> Unit = {},
): RustTimeline {
    return RustTimeline(
        inner = inner,
        mode = mode,
        systemClock = systemClock,
        joinedRoom = joinedRoom,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher,
        roomContentForwarder = roomContentForwarder,
        featureFlagsService = featureFlagsService,
        onNewSyncedEvent = onNewSyncedEvent,
    )
}
