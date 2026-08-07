/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aTimelineItemContentFactory
import io.element.android.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import io.element.android.features.messages.impl.threads.list.ThreadsListEvents
import io.element.android.features.messages.impl.threads.list.ThreadsListPresenter
import io.element.android.features.messages.impl.threads.list.aThreadListItem
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.threads.FakeThreadsListService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ThreadsListPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        createThreadsListPresenter().test {
            awaitItem().run {
                assertThat(threads).isEmpty()
                assertThat(roomId).isEqualTo(A_ROOM_ID)
                assertThat(roomName).isEqualTo(A_ROOM_NAME)
                assertThat(roomAvatarUrl).isEqualTo(AN_AVATAR_URL)
            }
        }
    }

    @Test
    fun `present - paginate`() = runTest {
        val paginateRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val threadsListService = FakeThreadsListService(paginate = paginateRecorder)
        val room = FakeJoinedRoom(threadsListService = threadsListService)
        createThreadsListPresenter(room).test {
            val initialItem = awaitItem()

            // Pagination is automatically triggered on start, so we should have one call to paginate already
            paginateRecorder.assertions().isCalledOnce()

            initialItem.eventSink(ThreadsListEvents.Paginate)

            // Simulate a pagination result
            threadsListService.emit(listOf(aThreadListItem()))

            // We should have a second call to paginate after the event is sent
            paginateRecorder.assertions().isCalledExactly(2)

            // And we receive the new items
            assertThat(awaitItem().threads).isNotEmpty()
        }
    }

    private fun createThreadsListPresenter(
        room: FakeJoinedRoom = FakeJoinedRoom(),
    ): ThreadsListPresenter {
        return ThreadsListPresenter(
            room = room,
            timelineItemContentFactory = aTimelineItemContentFactory(),
            messageSummaryFormatter = FakeMessageSummaryFormatter(),
            dateFormatter = FakeDateFormatter(),
        )
    }
}
