/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.matrix.api.permalink

import android.net.Uri
import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList

/**
 * This sealed class represents all the permalink cases.
 * You don't have to instantiate yourself but should use [PermalinkParser] instead.
 */
@Immutable
sealed interface PermalinkData {

    data class RoomLink(
        val roomIdOrAlias: String,
        val isRoomAlias: Boolean,
        val eventId: String?,
        val viaParameters: ImmutableList<String>
    ) : PermalinkData {
        fun getRoomId(): RoomId? {
            return roomIdOrAlias.takeIf { !isRoomAlias }?.let(::RoomId)
        }

        fun getRoomAlias(): String? {
            return roomIdOrAlias.takeIf { isRoomAlias }
        }
    }

    /*
     * &room_name=Team2
     * &room_avatar_url=mxc:
     * &inviter_name=bob
     */
    data class RoomEmailInviteLink(
        val roomId: String,
        val email: String,
        val signUrl: String,
        val roomName: String?,
        val roomAvatarUrl: String?,
        val inviterName: String?,
        val identityServer: String,
        val token: String,
        val privateKey: String,
        val roomType: String?
    ) : PermalinkData

    data class UserLink(val userId: String) : PermalinkData

    data class FallbackLink(val uri: Uri, val isLegacyGroupLink: Boolean = false) : PermalinkData
}
