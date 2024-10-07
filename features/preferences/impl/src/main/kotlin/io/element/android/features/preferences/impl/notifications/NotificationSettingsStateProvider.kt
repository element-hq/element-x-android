/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class NotificationSettingsStateProvider : PreviewParameterProvider<NotificationSettingsState> {
    override val values: Sequence<NotificationSettingsState>
        get() = sequenceOf(
            aValidNotificationSettingsState(systemNotificationsEnabled = false),
            aValidNotificationSettingsState(),
            aValidNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Loading),
            aValidNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Failure(Throwable("error"))),
            aValidNotificationSettingsState(
                availablePushDistributors = listOf("Firebase"),
                changeNotificationSettingAction = AsyncAction.Failure(Throwable("error")),
            ),
            aValidNotificationSettingsState(availablePushDistributors = listOf("Firebase")),
            aValidNotificationSettingsState(showChangePushProviderDialog = true),
            aValidNotificationSettingsState(currentPushDistributor = AsyncData.Loading()),
            aValidNotificationSettingsState(currentPushDistributor = AsyncData.Failure(Exception("Failed to change distributor"))),
            aInvalidNotificationSettingsState(),
            aInvalidNotificationSettingsState(fixFailed = true),
            aValidNotificationSettingsState(fullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(permissionGranted = false)),
            aValidNotificationSettingsState(appNotificationEnabled = false),
        )
}

fun aValidNotificationSettingsState(
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    atRoomNotificationsEnabled: Boolean = true,
    callNotificationsEnabled: Boolean = true,
    inviteForMeNotificationsEnabled: Boolean = true,
    systemNotificationsEnabled: Boolean = true,
    appNotificationEnabled: Boolean = true,
    currentPushDistributor: AsyncData<String> = AsyncData.Success("Firebase"),
    availablePushDistributors: List<String> = listOf("Firebase", "ntfy"),
    showChangePushProviderDialog: Boolean = false,
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
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
    eventSink = eventSink,
)
