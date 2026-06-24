/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet

open class SecurityAndPrivacyStateProvider : PreviewParameterProvider<SecurityAndPrivacyState> {
    override val values: Sequence<SecurityAndPrivacyState>
        get() = commonSecurityAndPrivacyStates(isSpace = false) +
            commonSecurityAndPrivacyStates(isSpace = true) +
            sequenceOf(
                aSecurityAndPrivacyState(
                    saveAction = AsyncAction.Loading,
                    isSpace = false,
                ),
                aSecurityAndPrivacyState(
                    saveAction = AsyncAction.Failure(SecurityAndPrivacyFailures.SaveFailed),
                    isSpace = false,
                ),
                aSecurityAndPrivacyState(
                    saveAction = AsyncAction.ConfirmingCancellation,
                    isSpace = false,
                ),
                aSecurityAndPrivacyState(
                    showEncryptionConfirmation = true,
                    isSpace = false,
                ),
            )
}

private fun commonSecurityAndPrivacyStates(isSpace: Boolean): Sequence<SecurityAndPrivacyState> = sequenceOf(
    aSecurityAndPrivacyState(isSpace = isSpace),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin,
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin
        ),
        isSpace = isSpace,
        isKnockEnabled = false,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            isEncrypted = false,
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.SpaceMember(persistentListOf())
        ),
        spaceSelectionMode = SpaceSelectionMode.Multiple,
        isSpace = isSpace,
        isKnockEnabled = false,
    ),
    aSecurityAndPrivacyState(
        spaceSelectionMode = SpaceSelectionMode.Multiple,
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(persistentListOf()),
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        spaceSelectionMode = SpaceSelectionMode.Multiple,
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(persistentListOf())
        ),
        isSpace = isSpace,
        isKnockEnabled = true,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            address = "#therapy:myserver.xyz"
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            isVisibleInRoomDirectory = AsyncData.Loading()
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            isVisibleInRoomDirectory = AsyncData.Success(true)
        ),
        isSpace = isSpace,
    ),
)

fun aSecurityAndPrivacySettings(
    roomAccess: SecurityAndPrivacyRoomAccess = SecurityAndPrivacyRoomAccess.InviteOnly,
    isEncrypted: Boolean = true,
    address: String? = null,
    historyVisibility: SecurityAndPrivacyHistoryVisibility = SecurityAndPrivacyHistoryVisibility.Shared,
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
    isKnockEnabled: Boolean = true,
    isSpace: Boolean = false,
    selectableJoinedSpaces: Set<SpaceRoom> = emptySet(),
    spaceSelectionMode: SpaceSelectionMode = SpaceSelectionMode.None,
    eventSink: (SecurityAndPrivacyEvent) -> Unit = {}
) = SecurityAndPrivacyState(
    editedSettings = editedSettings,
    savedSettings = savedSettings,
    homeserverName = homeserverName,
    showEnableEncryptionConfirmation = showEncryptionConfirmation,
    saveAction = saveAction,
    isKnockEnabled = isKnockEnabled,
    permissions = permissions,
    isSpace = isSpace,
    selectableJoinedSpaces = selectableJoinedSpaces.toImmutableSet(),
    spaceSelectionMode = spaceSelectionMode,
    eventSink = eventSink,
)
