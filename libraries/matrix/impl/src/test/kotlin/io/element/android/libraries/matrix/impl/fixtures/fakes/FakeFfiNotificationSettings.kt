/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomNotificationSettings
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate
import org.matrix.rustcomponents.sdk.RoomNotificationSettings

class FakeFfiNotificationSettings(
    private val roomNotificationSettings: RoomNotificationSettings = aRustRoomNotificationSettings(),
) : NotificationSettings(NoPointer) {
    private var delegate: NotificationSettingsDelegate? = null

    override fun setDelegate(delegate: NotificationSettingsDelegate?) {
        this.delegate = delegate
    }

    override suspend fun getRoomNotificationSettings(
        roomId: String,
        isEncrypted: Boolean,
        isOneToOne: Boolean,
    ): RoomNotificationSettings = roomNotificationSettings
}
