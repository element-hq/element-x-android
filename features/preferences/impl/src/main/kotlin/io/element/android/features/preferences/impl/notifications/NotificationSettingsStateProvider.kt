/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.pushproviders.api.Distributor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class NotificationSettingsStateProvider : PreviewParameterProvider<NotificationSettingsState> {
    override val values: Sequence<NotificationSettingsState>
        get() = sequenceOf(
            aValidNotificationSettingsState(systemNotificationsEnabled = false),
            aValidNotificationSettingsState(),
            aValidNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Loading),
            aValidNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Failure(RuntimeException("error"))),
            aValidNotificationSettingsState(
                availablePushDistributors = listOf(aDistributor("Firebase")),
                changeNotificationSettingAction = AsyncAction.Failure(RuntimeException("error")),
            ),
            aValidNotificationSettingsState(availablePushDistributors = listOf(aDistributor("Firebase"))),
            aValidNotificationSettingsState(showChangePushProviderDialog = true),
            aValidNotificationSettingsState(
                availablePushDistributors = listOf(
                    aDistributor("Firebase"),
                    aDistributor("ntfy", "app.id1"),
                    aDistributor("ntfy", "app.id2"),
                ),
                showChangePushProviderDialog = true,
            ),
            aValidNotificationSettingsState(currentPushDistributor = AsyncData.Loading()),
            aValidNotificationSettingsState(currentPushDistributor = AsyncData.Failure(Exception("Failed to change distributor"))),
            aInvalidNotificationSettingsState(),
            aInvalidNotificationSettingsState(fixFailed = true),
            aValidNotificationSettingsState(fullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(permissionGranted = false)),
            aValidNotificationSettingsState(appNotificationEnabled = false),
            aValidNotificationSettingsState(
                messageSound = NotificationSound.Custom("content://settings/system/notification_sound"),
                messageSoundDisplayName = "Pixel notification",
                callRingtone = NotificationSound.Custom("content://settings/system/ringtone"),
                callRingtoneDisplayName = "Pixel ringtone",
            ),
            aValidNotificationSettingsState(
                messageSound = NotificationSound.Silent,
                messageSoundDisplayName = "Silent",
                callRingtone = NotificationSound.Silent,
                callRingtoneDisplayName = "Silent",
            ),
        )
}

fun aValidNotificationSettingsState(
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    atRoomNotificationsEnabled: Boolean = true,
    callNotificationsEnabled: Boolean = true,
    inviteForMeNotificationsEnabled: Boolean = true,
    systemNotificationsEnabled: Boolean = true,
    appNotificationEnabled: Boolean = true,
    currentPushDistributor: AsyncData<Distributor> = AsyncData.Success(aDistributor("Firebase")),
    availablePushDistributors: List<Distributor> = listOf(
        aDistributor("Firebase"),
        aDistributor("ntfy"),
    ),
    showChangePushProviderDialog: Boolean = false,
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
    messageSound: NotificationSound = NotificationSound.SystemDefault,
    messageSoundDisplayName: String = "Default",
    callRingtone: NotificationSound = NotificationSound.SystemDefault,
    callRingtoneDisplayName: String = "Default",
    eventSink: (NotificationSettingsEvents) -> Unit = {},
) = NotificationSettingsState(
    matrixSettings = NotificationSettingsState.MatrixSettings.Valid(
        atRoomNotificationsEnabled = atRoomNotificationsEnabled,
        callNotificationsEnabled = callNotificationsEnabled,
        inviteForMeNotificationsEnabled = inviteForMeNotificationsEnabled,
        defaultGroupNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
        defaultOneToOneNotificationMode = RoomNotificationMode.ALL_MESSAGES,
    ),
    appSettings = NotificationSettingsState.AppSettings(
        systemNotificationsEnabled = systemNotificationsEnabled,
        appNotificationsEnabled = appNotificationEnabled,
    ),
    changeNotificationSettingAction = changeNotificationSettingAction,
    currentPushDistributor = currentPushDistributor,
    availablePushDistributors = availablePushDistributors.toImmutableList(),
    showChangePushProviderDialog = showChangePushProviderDialog,
    fullScreenIntentPermissionsState = fullScreenIntentPermissionsState,
    messageSound = messageSound,
    messageSoundDisplayName = messageSoundDisplayName,
    callRingtone = callRingtone,
    callRingtoneDisplayName = callRingtoneDisplayName,
    eventSink = eventSink,
)

fun aInvalidNotificationSettingsState(
    fixFailed: Boolean = false,
    eventSink: (NotificationSettingsEvents) -> Unit = {},
) = NotificationSettingsState(
    matrixSettings = NotificationSettingsState.MatrixSettings.Invalid(
        fixFailed = fixFailed,
    ),
    appSettings = NotificationSettingsState.AppSettings(
        systemNotificationsEnabled = false,
        appNotificationsEnabled = true,
    ),
    changeNotificationSettingAction = AsyncAction.Uninitialized,
    currentPushDistributor = AsyncData.Uninitialized,
    availablePushDistributors = persistentListOf(),
    showChangePushProviderDialog = false,
    fullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
    messageSound = NotificationSound.SystemDefault,
    messageSoundDisplayName = "Default",
    callRingtone = NotificationSound.SystemDefault,
    callRingtoneDisplayName = "Default",
    eventSink = eventSink,
)

fun aDistributor(
    name: String = "Name",
    value: String = "$name Value",
) = Distributor(
    value = value,
    name = name,
)
