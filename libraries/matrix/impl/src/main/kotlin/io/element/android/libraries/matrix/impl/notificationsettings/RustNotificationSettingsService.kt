/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notificationsettings

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.suspendLazy
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate
import org.matrix.rustcomponents.sdk.NotificationSettingsException
import timber.log.Timber

class RustNotificationSettingsService(
    client: Client,
    sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) : NotificationSettingsService {
    private val notificationSettings by suspendLazy(sessionCoroutineScope.coroutineContext + dispatchers.io) { client.getNotificationSettings() }
    private val _notificationSettingsChangeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val notificationSettingsChangeFlow: SharedFlow<Unit> = _notificationSettingsChangeFlow.asSharedFlow()

    private var notificationSettingsDelegate = object : NotificationSettingsDelegate {
        override fun settingsDidChange() {
            _notificationSettingsChangeFlow.tryEmit(Unit)
        }
    }

    suspend fun start() {
        notificationSettings.await().setDelegate(notificationSettingsDelegate)
    }

    suspend fun destroy() {
        notificationSettings.await().setDelegate(null)
    }

    override suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings> =
        runCatchingExceptions {
            notificationSettings.await().getRoomNotificationSettings(roomId.value, isEncrypted, isOneToOne).let(RoomNotificationSettingsMapper::map)
        }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode> =
        runCatchingExceptions {
            notificationSettings.await().getDefaultRoomNotificationMode(isEncrypted, isOneToOne).let(RoomNotificationSettingsMapper::mapMode)
        }

    override suspend fun setDefaultRoomNotificationMode(
        isEncrypted: Boolean,
        mode: RoomNotificationMode,
        isOneToOne: Boolean
    ): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            try {
                notificationSettings.await().setDefaultRoomNotificationMode(isEncrypted, isOneToOne, mode.let(RoomNotificationSettingsMapper::mapMode))
            } catch (exception: NotificationSettingsException.RuleNotFound) {
                // `setDefaultRoomNotificationMode` updates multiple rules including unstable rules (e.g. the polls push rules defined in the MSC3930)
                // since production home servers may not have these rules yet, we drop the RuleNotFound error
                Timber.w("Unable to find the rule: ${exception.ruleId}")
            }
        }
    }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().setRoomNotificationMode(roomId.value, mode.let(RoomNotificationSettingsMapper::mapMode))
        }
    }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().restoreDefaultRoomNotificationMode(roomId.value)
        }
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> = setRoomNotificationMode(roomId, RoomNotificationMode.MUTE)

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean) = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().unmuteRoom(roomId.value, isEncrypted, isOneToOne)
        }
    }

    override suspend fun isRoomMentionEnabled(): Result<Boolean> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().isRoomMentionEnabled()
        }
    }

    override suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().setRoomMentionEnabled(enabled)
        }
    }

    override suspend fun isCallEnabled(): Result<Boolean> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().isCallEnabled()
        }
    }

    override suspend fun setCallEnabled(enabled: Boolean): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().setCallEnabled(enabled)
        }
    }

    override suspend fun isInviteForMeEnabled(): Result<Boolean> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().isInviteForMeEnabled()
        }
    }

    override suspend fun setInviteForMeEnabled(enabled: Boolean): Result<Unit> = withContext(dispatchers.io) {
        runCatchingExceptions {
            notificationSettings.await().setInviteForMeEnabled(enabled)
        }
    }

    override suspend fun getRoomsWithUserDefinedRules(): Result<List<RoomId>> =
        runCatchingExceptions {
            notificationSettings.await().getRoomsWithUserDefinedRules(enabled = true).map(::RoomId)
        }

    override suspend fun canHomeServerPushEncryptedEventsToDevice(): Result<Boolean> =
        runCatchingExceptions {
            notificationSettings.await().canPushEncryptedEventToDevice()
        }

    override suspend fun getRawPushRules(): Result<String?> = runCatchingExceptions {
        notificationSettings.await().getRawPushRules()
    }
}
