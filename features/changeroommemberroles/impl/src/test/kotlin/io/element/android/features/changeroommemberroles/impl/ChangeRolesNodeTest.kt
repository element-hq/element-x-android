/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.matrix.api.room.RoomMember
import org.junit.Test

class ChangeRolesNodeTest {
    @Test
    fun `test toRoomMemberRole`() {
        assertThat(ChangeRoomMemberRolesListType.Admins.toRoomMemberRole())
            .isEqualTo(RoomMember.Role.Admin)
        assertThat(ChangeRoomMemberRolesListType.Moderators.toRoomMemberRole())
            .isEqualTo(RoomMember.Role.Moderator)
        assertThat(ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving.toRoomMemberRole())
            .isEqualTo(RoomMember.Role.Owner(false))
    }
}
