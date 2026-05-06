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
                // Sentinel URIs — previews shouldn't depend on the host's content provider.
                messageSound = NotificationSound.Custom("preview://message-sound"),
                messageSoundDisplayName = "Pixel notification",
                callRingtone = NotificationSound.Custom("preview://call-ringtone"),
                callRingtoneDisplayName = "Pixel ringtone",
            ),
            aValidNotificationSettingsState(
                messageSound = NotificationSound.Silent,
                messageSoundDisplayName = "Silent",
                callRingtone = NotificationSound.Silent,
                callRingtoneDisplayName = "Silent",
            ),
            aValidNotificationSettingsState(
                messageSoundCopyError = true,
                callRingtoneCopyError = false,
            ),
            aValidNotificationSettingsState(
                messageSound = NotificationSound.ElementDefault,
                messageSoundDisplayName = "Element default",
            ),
            aValidNotificationSettingsState(showMessageSoundDialog = true),
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
    messageSoundDisplayName: String = "Element default",
    messageSoundCopyError: Boolean = false,
    callRingtone: NotificationSound = NotificationSound.SystemDefault,
    callRingtoneDisplayName: String = "System default",
    callRingtoneCopyError: Boolean = false,
    showMessageSoundDialog: Boolean = false,
    pendingMessageSoundPickerLaunch: Int = 0,
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
    messageSound = NotificationSettingsState.SoundChannelUiState(
        sound = messageSound,
        displayName = messageSoundDisplayName,
        copyError = messageSoundCopyError,
    ),
    callRingtone = NotificationSettingsState.SoundChannelUiState(
        sound = callRingtone,
        displayName = callRingtoneDisplayName,
        copyError = callRingtoneCopyError,
    ),
    showMessageSoundDialog = showMessageSoundDialog,
    pendingMessageSoundPickerLaunch = pendingMessageSoundPickerLaunch,
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
    messageSound = NotificationSettingsState.SoundChannelUiState(
        sound = NotificationSound.SystemDefault,
        displayName = "System default",
        copyError = false,
    ),
    callRingtone = NotificationSettingsState.SoundChannelUiState(
        sound = NotificationSound.SystemDefault,
        displayName = "System default",
        copyError = false,
    ),
    showMessageSoundDialog = false,
    pendingMessageSoundPickerLaunch = 0,
    eventSink = eventSink,
)

fun aDistributor(
    name: String = "Name",
    value: String = "$name Value",
) = Distributor(
    value = value,
    name = name,
)
