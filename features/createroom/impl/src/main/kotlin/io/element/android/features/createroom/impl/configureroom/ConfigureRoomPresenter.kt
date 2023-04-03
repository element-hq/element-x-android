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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList

class ConfigureRoomPresenter @AssistedInject constructor(
    @Assisted val args: ConfigureRoomPresenterArgs,
) : Presenter<ConfigureRoomState> {

    @AssistedFactory
    interface Factory {
        fun create(args: ConfigureRoomPresenterArgs): ConfigureRoomPresenter
    }

    @Composable
    override fun present(): ConfigureRoomState {

        fun handleEvents(event: ConfigureRoomEvents) {
            when (event) {
                ConfigureRoomEvents.MyEvent -> Unit
            }
        }

        return ConfigureRoomState(
            selectedUsers = args.selectedUsers.toImmutableList(),
            avatarUri = null,
            eventSink = ::handleEvents,
        )
    }
}
