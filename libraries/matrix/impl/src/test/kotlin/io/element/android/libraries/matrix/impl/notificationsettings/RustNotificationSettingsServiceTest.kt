/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notificationsettings

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiNotificationSettings
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun `destroy destroys the notification settings once they have been created`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val sut = createRustNotificationSettingsService(
            notificationSettings = FakeFfiNotificationSettings(destroyResult = destroyResult),
        )
        sut.start()
        sut.destroy()
        destroyResult.assertions().isCalledOnce()
    }

    @Test
    fun `destroy does not suspend if the notification settings could not be created`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val sessionCoroutineScope = CoroutineScope(Job()).apply { cancel() }
        val sut = createRustNotificationSettingsService(
            notificationSettings = FakeFfiNotificationSettings(destroyResult = destroyResult),
            sessionCoroutineScope = sessionCoroutineScope,
        )
        // Trigger the creation, which will never complete since the session scope is cancelled
        backgroundScope.launch { sut.start() }
        runCurrent()
        sut.destroy()
        destroyResult.assertions().isNeverCalled()
    }

    @Test
    fun `destroy does nothing if the notification settings were never used`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val sut = createRustNotificationSettingsService(
            notificationSettings = FakeFfiNotificationSettings(destroyResult = destroyResult),
        )
        sut.destroy()
        destroyResult.assertions().isNeverCalled()
    }

    private fun TestScope.createRustNotificationSettingsService(
        notificationSettings: NotificationSettings = FakeFfiNotificationSettings(),
        sessionCoroutineScope: CoroutineScope = this,
    ) = RustNotificationSettingsService(
        client = FakeFfiClient(
            notificationSettings = notificationSettings,
        ),
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = testCoroutineDispatchers(),
    )
}
