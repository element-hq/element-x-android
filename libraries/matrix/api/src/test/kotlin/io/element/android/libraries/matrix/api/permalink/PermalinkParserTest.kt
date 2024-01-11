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

package io.element.android.libraries.matrix.api.permalink

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermalinkParserTest {
    @Test
    fun `parsing an invalid url returns a fallback link`() {
        val url = "https://element.io"
        assertThat(PermalinkParser.parse(url)).isInstanceOf(PermalinkData.FallbackLink::class.java)
    }

    @Test
    fun `parsing an invalid url with the right path but no content returns a fallback link`() {
        val url = "https://app.element.io/#/user"
        assertThat(PermalinkParser.parse(url)).isInstanceOf(PermalinkData.FallbackLink::class.java)
    }

    @Test
    fun `parsing an invalid url with the right path but empty content returns a fallback link`() {
        val url = "https://app.element.io/#/user/"
        assertThat(PermalinkParser.parse(url)).isInstanceOf(PermalinkData.FallbackLink::class.java)
    }

    @Test
    fun `parsing an invalid url with the right path but invalid content returns a fallback link`() {
        val url = "https://app.element.io/#/user/some%20user!"
        assertThat(PermalinkParser.parse(url)).isInstanceOf(PermalinkData.FallbackLink::class.java)
    }

    @Test
    fun `parsing a valid user url returns a user link`() {
        val url = "https://app.element.io/#/user/@test:matrix.org"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.UserLink(
                userId = "@test:matrix.org"
            )
        )
    }

    @Test
    fun `parsing a valid room id url returns a room link`() {
        val url = "https://app.element.io/#/room/!aBCD1234:matrix.org"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomLink(
                roomIdOrAlias = "!aBCD1234:matrix.org",
                isRoomAlias = false,
                eventId = null,
                viaParameters = persistentListOf(),
            )
        )
    }

    @Test
    fun `parsing a valid room id with event id url returns a room link`() {
        val url = "https://app.element.io/#/room/!aBCD1234:matrix.org/$1234567890abcdef:matrix.org"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomLink(
                roomIdOrAlias = "!aBCD1234:matrix.org",
                isRoomAlias = false,
                eventId = "\$1234567890abcdef:matrix.org",
                viaParameters = persistentListOf(),
            )
        )
    }

    @Test
    fun `parsing a valid room id with and invalid event id url returns a room link with no event id`() {
        val url = "https://app.element.io/#/room/!aBCD1234:matrix.org/1234567890abcdef:matrix.org"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomLink(
                roomIdOrAlias = "!aBCD1234:matrix.org",
                isRoomAlias = false,
                eventId = null,
                viaParameters = persistentListOf(),
            )
        )
    }

    @Test
    fun `parsing a valid room id with event id and via parameters url returns a room link`() {
        val url = "https://app.element.io/#/room/!aBCD1234:matrix.org/$1234567890abcdef:matrix.org?via=matrix.org&via=matrix.com"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomLink(
                roomIdOrAlias = "!aBCD1234:matrix.org",
                isRoomAlias = false,
                eventId = "\$1234567890abcdef:matrix.org",
                viaParameters = persistentListOf("matrix.org", "matrix.com"),
            )
        )
    }

    @Test
    fun `parsing a valid room alias url returns a room link`() {
        val url = "https://app.element.io/#/room/#element-android:matrix.org"
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomLink(
                roomIdOrAlias = "#element-android:matrix.org",
                isRoomAlias = true,
                eventId = null,
                viaParameters = persistentListOf(),
            )
        )
    }

    @Test
    fun `parsing a url with an invalid signurl returns a fallback link`() {
        // This url has no private key
        val url = "https://app.element.io/#/room/%21aBCDEF12345%3Amatrix.org" +
            "?email=testuser%40element.io" +
            "&signurl=https%3A%2F%2Fvector.im%2F_matrix%2Fidentity%2Fapi%2Fv1%2Fsign-ed25519%3Ftoken%3Da_token" +
            "&room_name=TestRoom" +
            "&room_avatar_url=" +
            "&inviter_name=User" +
            "&guest_access_token=" +
            "&guest_user_id=" +
            "&room_type="
        assertThat(PermalinkParser.parse(url)).isInstanceOf(PermalinkData.FallbackLink::class.java)
    }

    @Test
    fun `parsing a url with signurl returns a room email invite link`() {
        val url = "https://app.element.io/#/room/%21aBCDEF12345%3Amatrix.org" +
            "?email=testuser%40element.io" +
            "&signurl=https%3A%2F%2Fvector.im%2F_matrix%2Fidentity%2Fapi%2Fv1%2Fsign-ed25519%3Ftoken%3Da_token%26private_key%3Da_private_key" +
            "&room_name=TestRoom" +
            "&room_avatar_url=" +
            "&inviter_name=User" +
            "&guest_access_token=" +
            "&guest_user_id=" +
            "&room_type="
        assertThat(PermalinkParser.parse(url)).isEqualTo(
            PermalinkData.RoomEmailInviteLink(
                roomId = "!aBCDEF12345:matrix.org",
                email = "testuser@element.io",
                signUrl = "https://vector.im/_matrix/identity/api/v1/sign-ed25519?token=a_token&private_key=a_private_key",
                roomName = "TestRoom",
                roomAvatarUrl = "",
                inviterName = "User",
                identityServer = "vector.im",
                token = "a_token",
                privateKey = "a_private_key",
                roomType = "",
            )
        )
    }
}
