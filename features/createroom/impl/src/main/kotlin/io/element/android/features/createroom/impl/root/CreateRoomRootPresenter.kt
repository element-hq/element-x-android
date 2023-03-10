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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import javax.inject.Inject

class CreateRoomRootPresenter @Inject constructor() : Presenter<CreateRoomRootState> {

    @Composable
    override fun present(): CreateRoomRootState {
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val searchResults: MutableState<ImmutableList<MatrixUser>> = remember {
            mutableStateOf(persistentListOf())
        }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.UpdateSearchQuery -> searchQuery = event.query
                is CreateRoomRootEvents.StartDM -> handleStartDM(event.matrixUser)
                CreateRoomRootEvents.CreateRoom -> Unit // Todo Handle create room action
                CreateRoomRootEvents.InvitePeople -> Unit // Todo Handle invite people action
            }
        }

        LaunchedEffect(searchQuery) {
            searchResults.value = if (MatrixPatterns.isUserId(searchQuery)) {
                persistentListOf(MatrixUser(UserId(searchQuery)))
            } else {
                persistentListOf()
            }
            if (searchQuery.isNotEmpty()) {
                searchResults.value = performSearch(searchQuery)
            }
        }

        return CreateRoomRootState(
            eventSink = ::handleEvents,
            searchQuery = searchQuery,
            searchResults = searchResults.value,
        )
    }

    private fun performSearch(query: String): ImmutableList<MatrixUser> {
        val isMatrixId = MatrixPatterns.isUserId(query)
        val results = mutableListOf<MatrixUser>()// TODO trigger /search request
        if (isMatrixId && results.none { it.id.value == query }) {
            val getProfileResult: MatrixUser? = null // TODO trigger /profile request
            val profile = getProfileResult ?: MatrixUser(UserId(query))
            results.add(0, profile)
        }
        return results.toImmutableList()
    }

    private fun handleStartDM(matrixUser: MatrixUser) {
        Timber.d("handleStartDM: $matrixUser") // Todo handle start DM action
    }
}
