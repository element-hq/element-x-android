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

package io.element.android.features.poll.impl.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.poll.api.content.PollContentEvents
import io.element.android.features.poll.api.content.PollContentPresenter
import io.element.android.features.poll.api.content.PollContentState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent

class PollContentPresenterImpl @AssistedInject constructor(
    private val room: MatrixRoom,
    @Assisted private val content: PollContent,
) : PollContentPresenter {

    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    interface Factory: PollContentPresenter.Factory {
        override fun create(content: PollContent): PollContentPresenterImpl
    }

    @Composable
    override fun present(): PollContentState {

        var someRandomString: String by rememberSaveable { mutableStateOf("someRandomString") }
        var someState: Boolean by rememberSaveable { mutableStateOf(false) }

        fun handleEvents(event: PollContentEvents) {
            when (event) {
                is PollContentEvents.OnPollAnswerSelected -> {
                    someRandomString += "a"
                }
                is PollContentEvents.OnPollEndClicked -> {
                    someState = !someState
                }
            }
        }

        return PollContentState(
            content = content,
            someState = someState,
            someRandomString = someRandomString,
            eventSink = ::handleEvents
        )
    }
}
