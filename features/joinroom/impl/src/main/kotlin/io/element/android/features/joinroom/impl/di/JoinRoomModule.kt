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

package io.element.android.features.joinroom.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.invite.api.response.AcceptDeclineInvitePresenter
import io.element.android.features.joinroom.impl.JoinRoomPresenter
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import java.util.Optional

@Module
@ContributesTo(SessionScope::class)
object JoinRoomModule {
    @Provides
    fun providesJoinRoomPresenterFactory(
        roomListService: RoomListService,
        client: MatrixClient,
        acceptDeclineInvitePresenter: AcceptDeclineInvitePresenter,
    ): JoinRoomPresenter.Factory {
        return object : JoinRoomPresenter.Factory {
            override fun create(roomId: RoomId, roomDescription: Optional<RoomDescription>): JoinRoomPresenter {
                return JoinRoomPresenter(
                    roomId = roomId,
                    roomDescription = roomDescription,
                    matrixClient = client,
                    acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
                )
            }
        }
    }
}
