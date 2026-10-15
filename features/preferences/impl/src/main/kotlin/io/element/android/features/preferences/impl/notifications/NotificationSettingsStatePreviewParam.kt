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

open class NotificationSettingsStatePreviewParam : PreviewParameterProvider<NotificationSettingsState> {
    override val values: Sequence<NotificationSettingsState>
        get() = sequenceOf(
            // Uninitialized and invalid states
            listOf(
                anUninitializedNotificationSettingsState(),
                aInvalidNotificationSettingsState(),
                aInvalidNotificationSettingsState(
                    fixFailed = true
                ),
            ),
            // Global toggles
            listOf(
                aValidNotificationSettingsState(),
                aValidNotificationSettingsState(
                    systemNotificationsEnabled = false,
                ),
                aValidNotificationSettingsState(
                    appNotificationEnabled = false,
                ),
                aValidNotificationSettingsState(
                    fullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(permissionGranted = false),
                ),
            ),
            // Matrix settings
            listOf(
                aValidNotificationSettingsState(
                    showAllActivityInRoomList = true,
                    inviteForMeNotificationsEnabled = false,
                    atRoomNotificationsEnabled = false,
                ),
                aValidNotificationSettingsState(
                    defaultGroupNotificationMode = RoomNotificationMode.MUTE,
                    defaultOneToOneNotificationMode = null,
                ),
            ),
            // Change notification setting action
            listOf(
                aValidNotificationSettingsState(
                    changeNotificationSettingAction = AsyncAction.Loading,
                ),
                aValidNotificationSettingsState(
                    changeNotificationSettingAction = AsyncAction.Failure(RuntimeException("error")),
                ),
            ),
            // Sound and ringtone settings
            listOf(
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
                    messageSound = NotificationSound.ElementDefault,
                    messageSoundDisplayName = "Element default",
                ),
                aValidNotificationSettingsState(
                    showMessageSoundDialog = true,
                ),
                aValidNotificationSettingsState(
                    messageSound = NotificationSound.Custom("preview://message-sound"),
                    messageSoundDisplayName = "Pixel notification",
                    showMessageSoundDialog = true,
                ),
                aValidNotificationSettingsState(
                    showCallRingtoneDialog = true,
                ),
                aValidNotificationSettingsState(
                    callRingtone = NotificationSound.Custom("preview://call-ringtone"),
                    callRingtoneDisplayName = "Pixel ringtone",
                    showCallRingtoneDialog = true,
                ),
                aValidNotificationSettingsState(
                    messageSoundCopyError = true,
                ),
                aValidNotificationSettingsState(
                    callRingtoneCopyError = true,
                ),
            ),
            // Push distributors
            listOf(
                aDistributor("Firebase"),
                aDistributor("ntfy", "app.id1"),
                aDistributor("ntfy", "app.id2"),
            ).let { distributors ->
                listOf(
                    aValidNotificationSettingsState(
                        availablePushDistributors = distributors,
                    ),
                    aValidNotificationSettingsState(
                        availablePushDistributors = distributors,
                        showChangePushProviderDialog = true,
                    ),
                    aValidNotificationSettingsState(
                        availablePushDistributors = distributors,
                        currentPushDistributor = AsyncData.Loading(),
                    ),
                    aValidNotificationSettingsState(
                        availablePushDistributors = distributors,
                        currentPushDistributor = AsyncData.Failure(Exception("Failed to change distributor")),
                    ),
                )
            } + listOf(
                // Single distributor in failure state still shows the advanced settings
                aValidNotificationSettingsState(
                    currentPushDistributor = AsyncData.Failure(Exception("Failed to get distributor")),
                ),
            ),
        ).flatten()
}

fun aValidNotificationSettingsState(
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    atRoomNotificationsEnabled: Boolean = true,
    callNotificationsEnabled: Boolean = true,
    inviteForMeNotificationsEnabled: Boolean = true,
    defaultGroupNotificationMode: RoomNotificationMode? = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    defaultOneToOneNotificationMode: RoomNotificationMode? = RoomNotificationMode.ALL_MESSAGES,
    systemNotificationsEnabled: Boolean = true,
    appNotificationEnabled: Boolean = true,
    currentPushDistributor: AsyncData<Distributor> = AsyncData.Success(aDistributor("Firebase")),
    availablePushDistributors: List<Distributor> = listOf(
        aDistributor("Firebase"),
    ),
    showChangePushProviderDialog: Boolean = false,
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
    messageSound: NotificationSound = NotificationSound.SystemDefault,
    messageSoundDisplayName: String = "System default",
    messageSoundCopyError: Boolean = false,
    callRingtone: NotificationSound = NotificationSound.SystemDefault,
    callRingtoneDisplayName: String = "System default",
    callRingtoneCopyError: Boolean = false,
    showMessageSoundDialog: Boolean = false,
    showCallRingtoneDialog: Boolean = false,
    showAllActivityInRoomList: Boolean = false,
    pendingMessageSoundPickerLaunch: Int = 0,
    pendingCallRingtonePickerLaunch: Int = 0,
    eventSink: (NotificationSettingsEvent) -> Unit = {},
) = NotificationSettingsState(
    matrixSettings = NotificationSettingsState.MatrixSettings.Valid(
        atRoomNotificationsEnabled = atRoomNotificationsEnabled,
        callNotificationsEnabled = callNotificationsEnabled,
        inviteForMeNotificationsEnabled = inviteForMeNotificationsEnabled,
        defaultGroupNotificationMode = defaultGroupNotificationMode,
        defaultOneToOneNotificationMode = defaultOneToOneNotificationMode,
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
    showCallRingtoneDialog = showCallRingtoneDialog,
    pendingMessageSoundPickerLaunch = pendingMessageSoundPickerLaunch,
    pendingCallRingtonePickerLaunch = pendingCallRingtonePickerLaunch,
    showAllActivityInRoomList = showAllActivityInRoomList,
    eventSink = eventSink,
)

fun anUninitializedNotificationSettingsState() = aValidNotificationSettingsState().copy(
    matrixSettings = NotificationSettingsState.MatrixSettings.Uninitialized,
)

fun aInvalidNotificationSettingsState(
    fixFailed: Boolean = false,
    eventSink: (NotificationSettingsEvent) -> Unit = {},
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
    showCallRingtoneDialog = false,
    pendingMessageSoundPickerLaunch = 0,
    pendingCallRingtonePickerLaunch = 0,
    showAllActivityInRoomList = false,
    eventSink = eventSink,
)

fun aDistributor(
    name: String = "Name",
    value: String = "$name Value",
) = Distributor(
    value = value,
    name = name,
)
