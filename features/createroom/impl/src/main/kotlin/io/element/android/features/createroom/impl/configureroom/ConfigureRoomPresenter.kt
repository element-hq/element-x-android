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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.createroom.RoomVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfigureRoomPresenter @Inject constructor(
    private val dataStore: CreateRoomDataStore,
    private val matrixClient: MatrixClient,
) : Presenter<ConfigureRoomState> {

    @Composable
    override fun present(): ConfigureRoomState {
        val createRoomConfig = dataStore.getCreateRoomConfig().collectAsState(CreateRoomConfig())
        val isCreateButtonEnabled by remember(createRoomConfig.value.roomName, createRoomConfig.value.privacy) {
            derivedStateOf {
                createRoomConfig.value.roomName.isNullOrEmpty().not() && createRoomConfig.value.privacy != null
            }
        }

        val localCoroutineScope = rememberCoroutineScope()
        val createRoomAction: MutableState<Async<RoomId>> = remember { mutableStateOf(Async.Uninitialized) }

        fun createRoom(config: CreateRoomConfig) {
            createRoomAction.value = Async.Uninitialized
            localCoroutineScope.createRoom(config, createRoomAction)
        }

        fun handleEvents(event: ConfigureRoomEvents) {
            when (event) {
                is ConfigureRoomEvents.AvatarUriChanged -> dataStore.setAvatarUrl(event.uri?.toString())
                is ConfigureRoomEvents.RoomNameChanged -> dataStore.setRoomName(event.name)
                is ConfigureRoomEvents.TopicChanged -> dataStore.setTopic(event.topic)
                is ConfigureRoomEvents.RoomPrivacyChanged -> dataStore.setPrivacy(event.privacy)
                is ConfigureRoomEvents.RemoveFromSelection -> dataStore.selectedUserListDataStore.removeUserFromSelection(event.matrixUser)
                is ConfigureRoomEvents.CreateRoom -> createRoom(event.config)
                ConfigureRoomEvents.CancelCreateRoom -> createRoomAction.value = Async.Uninitialized
            }
        }

        return ConfigureRoomState(
            config = createRoomConfig.value,
            isCreateButtonEnabled = isCreateButtonEnabled,
            createRoomAction = createRoomAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createRoom(config: CreateRoomConfig, createRoomAction: MutableState<Async<RoomId>>) = launch {
        suspend {
            val params = CreateRoomParameters(
                name = config.roomName,
                topic = config.topic,
                isEncrypted = config.privacy == RoomPrivacy.Private,
                isDirect = false,
                visibility = if (config.privacy == RoomPrivacy.Public) RoomVisibility.PUBLIC else RoomVisibility.PRIVATE,
                preset = if (config.privacy == RoomPrivacy.Public) RoomPreset.PUBLIC_CHAT else RoomPreset.PRIVATE_CHAT,
                invite = config.invites.map { it.userId },
                avatar = config.avatarUrl,
            )
            matrixClient.createRoom(params).getOrThrow()
        }.execute(createRoomAction)
    }
}
