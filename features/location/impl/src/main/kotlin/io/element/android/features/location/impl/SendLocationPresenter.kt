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

package io.element.android.features.location.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.launch
import javax.inject.Inject

class SendLocationPresenter @Inject constructor(
    private val room: MatrixRoom,
) : Presenter<SendLocationState> {
    @Composable
    override fun present(): SendLocationState {

        val scope = rememberCoroutineScope()

        var mode by remember { mutableStateOf(SendLocationState.Mode.ALocation) }

        fun handleEvents(event: SendLocationEvents) {
            when (event) {
                is SendLocationEvents.ShareLocation -> scope.launch {
                    shareLocation(event)
                }
            }
        }

        return SendLocationState(
            mode = mode,
            eventSink = ::handleEvents,
        )
    }

    private suspend fun shareLocation(
        event: SendLocationEvents.ShareLocation
    ) {
        room.sendLocation(
            body = "Location at latitude: ${event.lat}, longitude: ${event.lng}",
            geoUri = "geo:${event.lat},${event.lng}",
        )
    }
}
