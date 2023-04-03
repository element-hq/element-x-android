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

package io.element.android.features.invitelist.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.element.android.features.invitelist.impl.model.InviteListInviteSummary
import io.element.android.features.invitelist.impl.model.InviteSender
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomSummary
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject

class InviteListPresenter @Inject constructor(
    private val client: MatrixClient,
) : Presenter<InviteListState> {

    @Composable
    override fun present(): InviteListState {
        val invites by client
            .invitesDataSource
            .roomSummaries()
            .collectAsState()

        return InviteListState(
            inviteList = invites.mapNotNull(::toInviteSummary).toPersistentList(),
        )
    }

    private fun toInviteSummary(roomSummary: RoomSummary): InviteListInviteSummary? {
        return when (roomSummary) {
            is RoomSummary.Filled -> roomSummary.details.run {
                val i = inviter
                val avatarData = if (isDirect && i != null)
                    AvatarData(
                        id = i.userId.value,
                        name = i.displayName,
                        url = i.avatarUrl,
                    )
                else
                    AvatarData(
                        id = roomId.value,
                        name = name,
                        url = avatarURLString
                    )

                val alias = if (isDirect)
                    inviter?.userId?.value
                else
                    canonicalAlias

                InviteListInviteSummary(
                    roomId = roomId,
                    roomName = name,
                    roomAlias = alias,
                    roomAvatarData = avatarData,
                    sender = if (isDirect) null else inviter?.let {
                        InviteSender(
                            userId = it.userId,
                            displayName = it.displayName ?: "",
                            avatarData = AvatarData(
                                id = it.userId.value,
                                name = it.displayName,
                                url = it.avatarUrl,
                            ),
                        )
                    }
                )
            }
            else -> null
        }
    }
}
