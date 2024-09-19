/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSession
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationProcessSetup
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeRustClient(
    private val userId: String = A_USER_ID.value,
    private val deviceId: String = A_DEVICE_ID.value,
    private val notificationClient: NotificationClient = FakeRustNotificationClient(),
    private val notificationSettings: NotificationSettings = FakeRustNotificationSettings(),
    private val encryption: Encryption = FakeRustEncryption(),
    private val session: Session = aRustSession(),
) : Client(NoPointer) {
    override fun userId(): String = userId
    override fun deviceId(): String = deviceId
    override suspend fun notificationClient(processSetup: NotificationProcessSetup) = notificationClient
    override fun getNotificationSettings(): NotificationSettings = notificationSettings
    override fun encryption(): Encryption = encryption
    override fun session(): Session = session
    override fun setDelegate(delegate: ClientDelegate?): TaskHandle = FakeRustTaskHandle()
    override fun cachedAvatarUrl(): String? = null
}
