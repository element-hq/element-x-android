/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.permalink

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize

/**
 * This sealed class represents all the permalink cases.
 * You don't have to instantiate yourself but should use [PermalinkParser] instead.
 */
@Immutable
@Parcelize
sealed interface PermalinkData : Parcelable {
    data class RoomLink(
        val roomIdOrAlias: RoomIdOrAlias,
        val eventId: EventId? = null,
        val threadId: ThreadId? = null,
        val viaParameters: ImmutableList<String> = persistentListOf()
    ) : PermalinkData

    /*
     * &room_name=Team2
     * &room_avatar_url=mxc:
     * &inviter_name=bob
     */
    data class RoomEmailInviteLink(
        val roomId: RoomId,
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

    data class UserLink(val userId: UserId) : PermalinkData

    data class FallbackLink(val uri: Uri, val isLegacyGroupLink: Boolean = false) : PermalinkData
}
