/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.securityandprivacy.permissions.SecurityAndPrivacyPermissions
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

open class SecurityAndPrivacyStateProvider : PreviewParameterProvider<SecurityAndPrivacyState> {
    override val values: Sequence<SecurityAndPrivacyState>
        get() = sequenceOf(
            aSecurityAndPrivacyState(),
            aSecurityAndPrivacyState(
                editedSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin
                )
            ),
            aSecurityAndPrivacyState(
                editedSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
                    isEncrypted = false,
                )
            ),
            aSecurityAndPrivacyState(
                savedSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.SpaceMember
                )
            ),
            aSecurityAndPrivacyState(
                editedSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
                    address = "#therapy:myserver.xyz"
                )
            ),
            aSecurityAndPrivacyState(
                editedSettings = aSecurityAndPrivacySettings(
                    isVisibleInRoomDirectory = AsyncData.Loading()
                )
            ),
            aSecurityAndPrivacyState(
                editedSettings = aSecurityAndPrivacySettings(
                    isVisibleInRoomDirectory = AsyncData.Success(true)
                )
            ),
            aSecurityAndPrivacyState(
                showEncryptionConfirmation = true
            ),
            aSecurityAndPrivacyState(
                saveAction = AsyncAction.Loading
            ),
        )
}

fun aSecurityAndPrivacySettings(
    roomAccess: SecurityAndPrivacyRoomAccess = SecurityAndPrivacyRoomAccess.InviteOnly,
    isEncrypted: Boolean = true,
    address: String? = null,
    historyVisibility: SecurityAndPrivacyHistoryVisibility = SecurityAndPrivacyHistoryVisibility.SinceSelection,
    isVisibleInRoomDirectory: AsyncData<Boolean> = AsyncData.Uninitialized,
) = SecurityAndPrivacySettings(
    roomAccess = roomAccess,
    isEncrypted = isEncrypted,
    address = address,
    historyVisibility = historyVisibility,
    isVisibleInRoomDirectory = isVisibleInRoomDirectory
)

fun aSecurityAndPrivacyState(
    savedSettings: SecurityAndPrivacySettings = aSecurityAndPrivacySettings(),
    editedSettings: SecurityAndPrivacySettings = savedSettings,
    homeserverName: String = "myserver.xyz",
    showEncryptionConfirmation: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    permissions: SecurityAndPrivacyPermissions = SecurityAndPrivacyPermissions(
        canChangeRoomAccess = true,
        canChangeHistoryVisibility = true,
        canChangeEncryption = true,
        canChangeRoomVisibility = true
    ),
    eventSink: (SecurityAndPrivacyEvents) -> Unit = {}
) = SecurityAndPrivacyState(
    editedSettings = editedSettings,
    savedSettings = savedSettings,
    homeserverName = homeserverName,
    showEnableEncryptionConfirmation = showEncryptionConfirmation,
    saveAction = saveAction,
    permissions = permissions,
    eventSink = eventSink
)
