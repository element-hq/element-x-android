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

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RoomAliasResolverPresenter @AssistedInject constructor(
    @Assisted roomAlias: String,
    private val matrixClient: MatrixClient,
) : Presenter<RoomAliasResolverState> {
    interface Factory {
        fun create(
            roomAlias: String,
        ): RoomAliasResolverPresenter
    }

    private val roomAlias = RoomAlias(roomAlias)

    @Composable
    override fun present(): RoomAliasResolverState {
        val coroutineScope = rememberCoroutineScope()
        val resolveState: MutableState<AsyncData<ResolvedRoomAlias>> = remember { mutableStateOf(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            resolveAlias(resolveState)
        }

        fun handleEvents(event: RoomAliasResolverEvents) {
            when (event) {
                RoomAliasResolverEvents.Retry -> coroutineScope.resolveAlias(resolveState)
            }
        }

        return RoomAliasResolverState(
            roomAlias = roomAlias,
            resolveState = resolveState.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.resolveAlias(resolveState: MutableState<AsyncData<ResolvedRoomAlias>>) = launch {
        suspend {
            matrixClient.resolveRoomAlias(roomAlias).getOrThrow()
        }.runCatchingUpdatingState(resolveState)
    }
}
