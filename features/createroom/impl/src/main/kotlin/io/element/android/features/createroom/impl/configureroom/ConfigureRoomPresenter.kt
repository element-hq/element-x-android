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

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class ConfigureRoomPresenter @Inject constructor(
    private val dataStore: CreateRoomDataStore,
) : Presenter<ConfigureRoomState> {

    @Composable
    override fun present(): ConfigureRoomState {
        val createRoomConfig = dataStore.getCreateRoomConfig().collectAsState(CreateRoomConfig())
        val isCreateButtonEnabled by rememberSaveable(createRoomConfig.value.roomName, createRoomConfig.value.privacy) {
            val enabled = createRoomConfig.value.roomName.isNullOrEmpty().not() && createRoomConfig.value.privacy != null
            mutableStateOf(enabled)
        }

        fun handleEvents(event: ConfigureRoomEvents) {
            when (event) {
                is ConfigureRoomEvents.AvatarUriChanged -> dataStore.setAvatarUrl(event.uri?.toString())
                is ConfigureRoomEvents.RoomNameChanged -> dataStore.setRoomName(event.name)
                is ConfigureRoomEvents.TopicChanged -> dataStore.setTopic(event.topic)
                is ConfigureRoomEvents.RoomPrivacyChanged -> dataStore.setPrivacy(event.privacy)
                is ConfigureRoomEvents.RemoveFromSelection -> dataStore.selectedUserListDataStore.removeUserFromSelection(event.matrixUser)
                ConfigureRoomEvents.CreateRoom -> Unit // TODO
            }
        }

        return ConfigureRoomState(
            createRoomConfig.value,
            isCreateButtonEnabled = isCreateButtonEnabled,
            eventSink = ::handleEvents,
        )
    }
}
