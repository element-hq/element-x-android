/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.roomdetails.edition

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.ui.AvatarAction
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.edition.RoomDetailsEditionPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomDetailsEditionPresenterTest {

    private lateinit var fakePickerProvider: FakePickerProvider
    private lateinit var fakeMediaPreProcessor: FakeMediaPreProcessor

    @Before
    fun setup() {
        fakePickerProvider = FakePickerProvider()
        fakeMediaPreProcessor = FakeMediaPreProcessor()
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun aRoomDetailsEditionPresenter(room: MatrixRoom): RoomDetailsEditionPresenter {
        return RoomDetailsEditionPresenter(
            room = room,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
        )
    }

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom()
        val presenter = aRoomDetailsEditionPresenter(room)

        val roomAvatarUri: Uri = mockk()
        every { Uri.parse(room.avatarUrl) } returns roomAvatarUri

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(room.roomId.value)
            assertThat(initialState.roomName).isEqualTo(room.name)
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            assertThat(initialState.roomTopic).isEqualTo(room.topic.orEmpty())
            assertThat(initialState.avatarActions).containsExactly(
                AvatarAction.ChoosePhoto,
                AvatarAction.TakePhoto,
                AvatarAction.Remove
            )
            assertThat(initialState.saveButtonEnabled).isEqualTo(false)
            assertThat(initialState.saveAction).isInstanceOf(Async.Uninitialized::class.java)
        }
    }
}
