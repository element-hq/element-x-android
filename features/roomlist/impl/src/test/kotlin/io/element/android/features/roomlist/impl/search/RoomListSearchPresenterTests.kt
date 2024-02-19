/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl.search

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLastMessageFormatter
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListSearchPresenterTests {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomListSearchPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
                assertThat(state.query).isEmpty()
                assertThat(state.results).isEmpty()
            }
        }
    }

    @Test
    fun `present - toggle search visibility`() = runTest {
        val presenter = createRoomListSearchPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
                state.eventSink(RoomListSearchEvents.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isTrue()
                state.eventSink(RoomListSearchEvents.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
            }
        }
    }

    @Test
    fun `present - query search changes`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListSearchPresenter(roomListService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let { state ->
                assertThat(
                    roomListService.allRooms.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.all(
                        RoomListFilter.None,
                    )
                )
                state.eventSink(RoomListSearchEvents.QueryChanged("Search"))
            }
            awaitItem().let { state ->
                assertThat(state.query).isEqualTo("Search")
                assertThat(
                    roomListService.allRooms.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.all(
                        RoomListFilter.NonLeft,
                        RoomListFilter.NormalizedMatchRoomName("Search")
                    )
                )
                state.eventSink(RoomListSearchEvents.ClearQuery)
            }
            awaitItem().let { state ->
                assertThat(state.query).isEmpty()
                assertThat(
                    roomListService.allRooms.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.all(
                        RoomListFilter.None,
                    )
                )
            }
        }
    }

    @Test
    fun `present - room list changes`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListSearchPresenter(roomListService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let { state ->
                assertThat(state.results).isEmpty()
            }
            roomListService.postAllRooms(
                listOf(
                    RoomSummary.Empty("1"),
                    aRoomSummaryFilled()
                )
            )
            awaitItem().let { state ->
                assertThat(state.results).hasSize(1)
            }
            roomListService.postAllRooms(emptyList())
            awaitItem().let { state ->
                assertThat(state.results).isEmpty()
            }
        }
    }
}

fun TestScope.createRoomListSearchPresenter(
    roomListService: RoomListService = FakeRoomListService(),
): RoomListSearchPresenter {
    return RoomListSearchPresenter(
        roomListService = roomListService,
        roomSummaryFactory = RoomListRoomSummaryFactory(
            lastMessageTimestampFormatter = FakeLastMessageTimestampFormatter(),
            roomLastMessageFormatter = FakeRoomLastMessageFormatter(),
            ),
        coroutineDispatchers = testCoroutineDispatchers(),
    )
}
