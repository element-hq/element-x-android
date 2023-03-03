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

package io.element.android.samples.minimal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.element.android.features.roomlist.RoomListPresenter
import io.element.android.features.roomlist.RoomListView
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.util.Locale

class RoomListScreen(
    private val matrixClient: MatrixClient
) {

    private val clock = Clock.System
    private val locale = Locale.getDefault()
    private val timeZone = TimeZone.currentSystemDefault()
    private val dateTimeProvider = LocalDateTimeProvider(clock, timeZone)
    private val dateFormatters = DateFormatters(locale, clock, timeZone)
    private val presenter = RoomListPresenter(matrixClient, DefaultLastMessageFormatter(dateTimeProvider, dateFormatters))

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        fun onRoomClicked(roomId: RoomId) {
            val room = matrixClient.getRoom(roomId)!!
            val timeline = room.timeline()
            Singleton.appScope.launch {
                timeline.apply {
                    initialize()
                    paginateBackwards(20, 50)
                    dispose()
                }
            }
        }

        val state = presenter.present()
        RoomListView(
            state = state,
            modifier = modifier,
            onRoomClicked = ::onRoomClicked,
        )

        DisposableEffect(Unit) {
            matrixClient.startSync()
            onDispose {
                matrixClient.stopSync()
            }
        }
    }
}
