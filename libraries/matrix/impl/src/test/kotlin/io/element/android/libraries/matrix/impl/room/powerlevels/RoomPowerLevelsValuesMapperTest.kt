/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.powerlevels

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomPowerLevelsValues
import org.junit.Test

class RoomPowerLevelsValuesMapperTest {
    @Test
    fun `test that mapping of RoomPowerLevelsValues is correct`() {
        assertThat(
            RoomPowerLevelsValuesMapper.map(
                aRustRoomPowerLevelsValues(
                    ban = 1,
                    invite = 2,
                    kick = 3,
                    redact = 4,
                    eventsDefault = 5,
                    stateDefault = 6,
                    usersDefault = 7,
                    roomName = 8,
                    roomAvatar = 9,
                    roomTopic = 10,
                    spaceChild = 11,
                )
            )
        ).isEqualTo(
            RoomPowerLevelsValues(
                ban = 1,
                invite = 2,
                kick = 3,
                sendEvents = 5,
                redactEvents = 4,
                roomName = 8,
                roomAvatar = 9,
                roomTopic = 10,
                spaceChild = 11,
            )
        )
    }
}
