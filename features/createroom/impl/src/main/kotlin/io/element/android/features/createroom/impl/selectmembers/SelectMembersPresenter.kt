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

package io.element.android.features.createroom.impl.selectmembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.ui.model.MatrixUser
import javax.inject.Inject

// TODO add unit tests
class SelectMembersPresenter @Inject constructor() : Presenter<SelectMembersState> {

    @Composable
    override fun present(): SelectMembersState {
        val selectedUsers = rememberSaveable { mutableStateOf(emptyList<MatrixUser>()) }

        fun handleEvents(event: SelectMembersEvents) {
            when (event) {
                is SelectMembersEvents.AddToSelection -> selectedUsers.value = mutableListOf<MatrixUser>().also {
                    it.addAll(selectedUsers.value.plus(event.matrixUser))
                }
                is SelectMembersEvents.RemoveFromSelection -> selectedUsers.value = mutableListOf<MatrixUser>().also {
                    it.addAll(selectedUsers.value.minus(event.matrixUser))
                }
            }
        }

        return SelectMembersState(
            selectedUsers = selectedUsers.value,
            eventSink = ::handleEvents,
        )
    }
}
