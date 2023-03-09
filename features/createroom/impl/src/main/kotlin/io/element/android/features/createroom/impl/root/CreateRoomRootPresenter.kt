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

package io.element.android.features.createroom.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bumble.appyx.core.collections.immutableListOf
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import javax.inject.Inject

class CreateRoomRootPresenter @Inject constructor() : Presenter<CreateRoomRootState> {

    @Composable
    override fun present(): CreateRoomRootState {

        var searchQuery by rememberSaveable { mutableStateOf("") }
        val searchResults = if (MatrixPatterns.isUserId(searchQuery)) {
            immutableListOf(MatrixUser(UserId(searchQuery)))
        } else {
            immutableListOf()
        }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                CreateRoomRootEvents.CreateRoom -> Unit // Todo Handle create room action
                CreateRoomRootEvents.InvitePeople -> Unit // Todo Handle invite people action
                is CreateRoomRootEvents.UpdateSearchQuery -> {
                    searchQuery = event.query
                }
            }
        }

        return CreateRoomRootState(
            eventSink = ::handleEvents,
            searchQuery = searchQuery,
            searchResults = searchResults,
        )
    }
}
