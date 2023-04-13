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

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember

class RoomMemberDetailsPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    @Assisted private val roomMember: RoomMember,
) : Presenter<RoomMemberDetailsState> {

    interface Factory {
        fun create(roomMember: RoomMember): RoomMemberDetailsPresenter
    }

    @Composable
    override fun present(): RoomMemberDetailsState {

//        fun handleEvents(event: RoomMemberDetailsEvents) {
//            when (event) {
//            }
//        }

        return RoomMemberDetailsState(
            userId = roomMember.userId,
            userName = roomMember.displayName,
            avatarUrl = roomMember.avatarUrl,
            isBlocked = roomMember.isIgnored,
//            eventSink = ::handleEvents
        )
    }
}
