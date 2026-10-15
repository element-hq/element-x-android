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
import kotlinx.collections.immutable.toImmutableList

open class NotificationSettingsStatePreviewParam : PreviewParameterProvider<NotificationSettingsState> {
    override val values: Sequence<NotificationSettingsState>
        get() = sequenceOf(
            // Uninitialized and invalid states
            listOf(
                aNotificationSettingsState(
                    matrixSettings = NotificationSettingsState.MatrixSettings.Uninitialized,
                ),
                aNotificationSettingsState(
                    matrixSettings = anInvalidMatrixSettings(),
                ),
                aNotificationSettingsState(
                    matrixSettings = anInvalidMatrixSettings(fixFailed = true),
                ),
            ),
            // Global toggles
            listOf(
                aNotificationSettingsState(),
                aNotificationSettingsState(
                    appSettings = anAppSettings(systemNotificationsEnabled = false),
                ),
                aNotificationSettingsState(
                    appSettings = anAppSettings(appNotificationsEnabled = false),
                ),
                aNotificationSettingsState(
                    fullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(permissionGranted = false),
                ),
            ),
            // Matrix settings
            listOf(
                aNotificationSettingsState(
                    matrixSettings = aValidMatrixSettings(
                        inviteForMeNotificationsEnabled = false,
                        atRoomNotificationsEnabled = false,
                    ),
                    showAllActivityInRoomList = true,
                ),
                aNotificationSettingsState(
                    matrixSettings = aValidMatrixSettings(
                        defaultGroupNotificationMode = RoomNotificationMode.MUTE,
                        defaultOneToOneNotificationMode = null,
                    ),
                ),
            ),
            // Change notification setting action
            listOf(
                aNotificationSettingsState(
                    changeNotificationSettingAction = AsyncAction.Loading,
                ),
                aNotificationSettingsState(
                    changeNotificationSettingAction = AsyncAction.Failure(RuntimeException("error")),
                ),
            ),
            // Sound and ringtone settings
            listOf(
                aNotificationSettingsState(
                    // Sentinel URIs — previews shouldn't depend on the host's content provider.
                    messageSound = aSoundChannelUiState(
                        sound = NotificationSound.Custom("preview://message-sound"),
                        displayName = "Pixel notification",
                    ),
                    callRingtone = aSoundChannelUiState(
                        sound = NotificationSound.Custom("preview://call-ringtone"),
                        displayName = "Pixel ringtone",
                    ),
                ),
                aNotificationSettingsState(
                    messageSound = aSoundChannelUiState(
                        sound = NotificationSound.Silent,
                        displayName = "Silent",
                    ),
                    callRingtone = aSoundChannelUiState(
                        sound = NotificationSound.Silent,
                        displayName = "Silent",
                    ),
                ),
                aNotificationSettingsState(
                    messageSound = aSoundChannelUiState(
                        sound = NotificationSound.ElementDefault,
                        displayName = "Element default",
                    ),
                ),
                aNotificationSettingsState(
                    showMessageSoundDialog = true,
                ),
                aNotificationSettingsState(
                    messageSound = aSoundChannelUiState(
                        sound = NotificationSound.Custom("preview://message-sound"),
                        displayName = "Pixel notification",
                    ),
                    showMessageSoundDialog = true,
                ),
                aNotificationSettingsState(
                    showCallRingtoneDialog = true,
                ),
                aNotificationSettingsState(
                    callRingtone = aSoundChannelUiState(
                        sound = NotificationSound.Custom("preview://call-ringtone"),
                        displayName = "Pixel ringtone",
                    ),
                    showCallRingtoneDialog = true,
                ),
                aNotificationSettingsState(
                    messageSound = aSoundChannelUiState(copyError = true),
                ),
                aNotificationSettingsState(
                    callRingtone = aSoundChannelUiState(copyError = true),
                ),
            ),
            // Push distributors
            listOf(
                aDistributor("Firebase"),
                aDistributor("ntfy", "app.id1"),
                aDistributor("ntfy", "app.id2"),
            ).let { distributors ->
                listOf(
                    aNotificationSettingsState(
                        availablePushDistributors = distributors,
                    ),
                    aNotificationSettingsState(
                        availablePushDistributors = distributors,
                        showChangePushProviderDialog = true,
                    ),
                    aNotificationSettingsState(
                        availablePushDistributors = distributors,
                        currentPushDistributor = AsyncData.Loading(),
                    ),
                    aNotificationSettingsState(
                        availablePushDistributors = distributors,
                        currentPushDistributor = AsyncData.Failure(Exception("Failed to change distributor")),
                    ),
                )
            } + listOf(
                // Single distributor in failure state still shows the advanced settings
                aNotificationSettingsState(
                    currentPushDistributor = AsyncData.Failure(Exception("Failed to get distributor")),
                ),
            ),
        ).flatten()
}

fun aNotificationSettingsState(
    matrixSettings: NotificationSettingsState.MatrixSettings = aValidMatrixSettings(),
    appSettings: NotificationSettingsState.AppSettings = anAppSettings(),
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    currentPushDistributor: AsyncData<Distributor> = AsyncData.Success(aDistributor("Firebase")),
    availablePushDistributors: List<Distributor> = listOf(
        aDistributor("Firebase"),
    ),
    showChangePushProviderDialog: Boolean = false,
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
    messageSound: NotificationSettingsState.SoundChannelUiState = aSoundChannelUiState(),
    callRingtone: NotificationSettingsState.SoundChannelUiState = aSoundChannelUiState(),
    showMessageSoundDialog: Boolean = false,
    showCallRingtoneDialog: Boolean = false,
    pendingMessageSoundPickerLaunch: Int = 0,
    pendingCallRingtonePickerLaunch: Int = 0,
    showAllActivityInRoomList: Boolean = false,
    eventSink: (NotificationSettingsEvent) -> Unit = {},
) = NotificationSettingsState(
    matrixSettings = matrixSettings,
    appSettings = appSettings,
    changeNotificationSettingAction = changeNotificationSettingAction,
    currentPushDistributor = currentPushDistributor,
    availablePushDistributors = availablePushDistributors.toImmutableList(),
    showChangePushProviderDialog = showChangePushProviderDialog,
    fullScreenIntentPermissionsState = fullScreenIntentPermissionsState,
    messageSound = messageSound,
    callRingtone = callRingtone,
    showMessageSoundDialog = showMessageSoundDialog,
    showCallRingtoneDialog = showCallRingtoneDialog,
    pendingMessageSoundPickerLaunch = pendingMessageSoundPickerLaunch,
    pendingCallRingtonePickerLaunch = pendingCallRingtonePickerLaunch,
    showAllActivityInRoomList = showAllActivityInRoomList,
    eventSink = eventSink,
)

fun aValidMatrixSettings(
    atRoomNotificationsEnabled: Boolean = true,
    callNotificationsEnabled: Boolean = true,
    inviteForMeNotificationsEnabled: Boolean = true,
    defaultGroupNotificationMode: RoomNotificationMode? = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    defaultOneToOneNotificationMode: RoomNotificationMode? = RoomNotificationMode.ALL_MESSAGES,
) = NotificationSettingsState.MatrixSettings.Valid(
    atRoomNotificationsEnabled = atRoomNotificationsEnabled,
    callNotificationsEnabled = callNotificationsEnabled,
    inviteForMeNotificationsEnabled = inviteForMeNotificationsEnabled,
    defaultGroupNotificationMode = defaultGroupNotificationMode,
    defaultOneToOneNotificationMode = defaultOneToOneNotificationMode,
)

fun anInvalidMatrixSettings(
    fixFailed: Boolean = false,
) = NotificationSettingsState.MatrixSettings.Invalid(
    fixFailed = fixFailed,
)

fun anAppSettings(
    systemNotificationsEnabled: Boolean = true,
    appNotificationsEnabled: Boolean = true,
) = NotificationSettingsState.AppSettings(
    systemNotificationsEnabled = systemNotificationsEnabled,
    appNotificationsEnabled = appNotificationsEnabled,
)

fun aSoundChannelUiState(
    sound: NotificationSound = NotificationSound.SystemDefault,
    displayName: String = "System default",
    copyError: Boolean = false,
) = NotificationSettingsState.SoundChannelUiState(
    sound = sound,
    displayName = displayName,
    copyError = copyError,
)

fun aDistributor(
    name: String = "Name",
    value: String = "$name Value",
) = Distributor(
    value = value,
    name = name,
)
