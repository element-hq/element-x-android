/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSession
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationProcessSetup
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.PusherIdentifiers
import org.matrix.rustcomponents.sdk.PusherKind
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.SyncServiceBuilder
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.UnableToDecryptDelegate

class FakeRustClient(
    private val userId: String = A_USER_ID.value,
    private val deviceId: String = A_DEVICE_ID.value,
    private val notificationClient: NotificationClient = FakeRustNotificationClient(),
    private val notificationSettings: NotificationSettings = FakeRustNotificationSettings(),
    private val encryption: Encryption = FakeRustEncryption(),
    private val session: Session = aRustSession(),
    private val clearCachesResult: () -> Unit = { lambdaError() },
    private val withUtdHook: (UnableToDecryptDelegate) -> Unit = { lambdaError() },
    private val closeResult: () -> Unit = {},
) : Client(NoPointer) {
    override fun userId(): String = userId
    override fun deviceId(): String = deviceId
    override suspend fun notificationClient(processSetup: NotificationProcessSetup) = notificationClient
    override suspend fun getNotificationSettings(): NotificationSettings = notificationSettings
    override fun encryption(): Encryption = encryption
    override fun session(): Session = session
    override fun setDelegate(delegate: ClientDelegate?): TaskHandle = FakeRustTaskHandle()
    override suspend fun cachedAvatarUrl(): String? = null
    override suspend fun restoreSession(session: Session) = Unit
    override fun syncService(): SyncServiceBuilder = FakeRustSyncServiceBuilder()
    override fun roomDirectorySearch(): RoomDirectorySearch = FakeRustRoomDirectorySearch()
    override suspend fun setPusher(
        identifiers: PusherIdentifiers,
        kind: PusherKind,
        appDisplayName: String,
        deviceDisplayName: String,
        profileTag: String?,
        lang: String,
    ) = Unit

    override suspend fun deletePusher(identifiers: PusherIdentifiers) = Unit
    override suspend fun clearCaches() = simulateLongTask { clearCachesResult() }
    override suspend fun setUtdDelegate(utdDelegate: UnableToDecryptDelegate) = withUtdHook(utdDelegate)
    override fun close() = closeResult()
}
