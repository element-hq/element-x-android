package io.element.android.x.matrix.permalink

import android.net.Uri

/**
 * This sealed class represents all the permalink cases.
 * You don't have to instantiate yourself but should use [PermalinkParser] instead.
 */
sealed class PermalinkData {

    data class RoomLink(
        val roomIdOrAlias: String,
        val isRoomAlias: Boolean,
        val eventId: String?,
        val viaParameters: List<String>
    ) : PermalinkData()

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
    ) : PermalinkData()

    data class UserLink(val userId: String) : PermalinkData()

    data class FallbackLink(val uri: Uri, val isLegacyGroupLink: Boolean = false) : PermalinkData()
}
