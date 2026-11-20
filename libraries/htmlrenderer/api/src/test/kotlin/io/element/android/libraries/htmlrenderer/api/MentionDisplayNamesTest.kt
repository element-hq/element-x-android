/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Test

class MentionDisplayNamesTest {
    private val alice = UserId("@alice:example.org")
    private val room = RoomAlias("#room:example.org").toRoomIdOrAlias()
    private val displayNames = MentionDisplayNames(
        users = persistentMapOf(alice to "Alice"),
        rooms = persistentMapOf(room to "A room"),
        userStatuses = persistentMapOf(alice to "🎧", UserId("@carol:example.org") to "🌴"),
    )

    @Test
    fun `a user mention uses the member display name if known`() {
        val mention = MentionNodeContent.User(displayText = alice.value, userId = alice, permalinkUrl = "")
        assertThat(displayNames.displayTextFor(mention)).isEqualTo("@Alice")
    }

    @Test
    fun `a user mention keeps its display text if the member is unknown`() {
        val bob = UserId("@bob:example.org")
        val mention = MentionNodeContent.User(displayText = "Bob", userId = bob, permalinkUrl = "")
        assertThat(displayNames.displayTextFor(mention)).isEqualTo("Bob")
    }

    @Test
    fun `a room mention uses the room name if known`() {
        val mention = MentionNodeContent.Room(displayText = room.identifier, roomIdOrAlias = room, permalinkUrl = "")
        assertThat(displayNames.displayTextFor(mention)).isEqualTo("#A room")
    }

    @Test
    fun `a room mention keeps its display text if the room is unknown`() {
        val otherRoom = RoomAlias("#other:example.org").toRoomIdOrAlias()
        val mention = MentionNodeContent.Room(displayText = otherRoom.identifier, roomIdOrAlias = otherRoom, permalinkUrl = "")
        assertThat(displayNames.displayTextFor(mention)).isEqualTo("#other:example.org")
    }

    @Test
    fun `an everyone mention always keeps its display text`() {
        val mention = MentionNodeContent.Everyone(displayText = "@room")
        assertThat(displayNames.displayTextFor(mention)).isEqualTo("@room")
    }

    @Test
    fun `the pill text of a user with a status ends with the status emoji`() {
        val mention = MentionNodeContent.User(displayText = alice.value, userId = alice, permalinkUrl = "")
        assertThat(displayNames.pillTextFor(mention)).isEqualTo("@Alice 🎧")
    }

    @Test
    fun `the status emoji is added even if the display name is unknown`() {
        val carol = UserId("@carol:example.org")
        val mention = MentionNodeContent.User(displayText = carol.value, userId = carol, permalinkUrl = "")
        assertThat(displayNames.pillTextFor(mention)).isEqualTo("@carol:example.org 🌴")
    }

    @Test
    fun `the pill text without status is the display text`() {
        val bob = UserId("@bob:example.org")
        assertThat(displayNames.pillTextFor(MentionNodeContent.User(displayText = "Bob", userId = bob, permalinkUrl = ""))).isEqualTo("Bob")
        assertThat(displayNames.pillTextFor(MentionNodeContent.Room(displayText = room.identifier, roomIdOrAlias = room, permalinkUrl = "")))
            .isEqualTo("#A room")
        assertThat(displayNames.pillTextFor(MentionNodeContent.Everyone(displayText = "@room"))).isEqualTo("@room")
    }
}
