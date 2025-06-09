/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notificationsettings

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiNotificationSettings
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.NotificationSettings

class RustNotificationSettingsServiceTest {
    @Test
    fun test() = runTest {
        val sut = createRustNotificationSettingsService()
        val result = sut.getRoomNotificationSettings(
            roomId = A_ROOM_ID,
            isEncrypted = true,
            isOneToOne = true,
        ).getOrNull()!!
        assertThat(result.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        assertThat(result.isDefault).isTrue()
    }

    private fun TestScope.createRustNotificationSettingsService(
        notificationSettings: NotificationSettings = FakeFfiNotificationSettings(),
    ) = RustNotificationSettingsService(
        client = FakeFfiClient(
            notificationSettings = notificationSettings,
        ),
        sessionCoroutineScope = this,
        dispatchers = testCoroutineDispatchers(),
    )
}
