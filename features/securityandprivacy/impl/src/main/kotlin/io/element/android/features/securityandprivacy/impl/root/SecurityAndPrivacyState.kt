/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.toImmutableSet

data class SecurityAndPrivacyState(
    // the settings that are currently applied on the room.
    val savedSettings: SecurityAndPrivacySettings,
    // the settings the user wants to apply.
    val editedSettings: SecurityAndPrivacySettings,
    val homeserverName: String,
    val showEnableEncryptionConfirmation: Boolean,
    val isKnockEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val isSpace: Boolean,
    private val permissions: SecurityAndPrivacyPermissions,
    val eventSink: (SecurityAndPrivacyEvent) -> Unit
) {
    val canBeSaved = savedSettings != editedSettings

    val availableHistoryVisibilities = buildSet {
        add(SecurityAndPrivacyHistoryVisibility.SinceSelection)
        if (editedSettings.roomAccess == SecurityAndPrivacyRoomAccess.Anyone && !editedSettings.isEncrypted) {
            add(SecurityAndPrivacyHistoryVisibility.Anyone)
        } else {
            add(SecurityAndPrivacyHistoryVisibility.SinceInvite)
        }
    }.toImmutableSet()

    val showRoomAccessSection = permissions.canChangeRoomAccess

    val showRoomVisibilitySections = permissions.canChangeRoomVisibility &&
        editedSettings.roomAccess.canConfigureRoomVisibility()

    val showHistoryVisibilitySection = permissions.canChangeHistoryVisibility && !isSpace
    val showEncryptionSection = permissions.canChangeEncryption && !isSpace
}

data class SecurityAndPrivacySettings(
    val roomAccess: SecurityAndPrivacyRoomAccess,
    val isEncrypted: Boolean,
    val historyVisibility: SecurityAndPrivacyHistoryVisibility,
    val address: String?,
    val isVisibleInRoomDirectory: AsyncData<Boolean>
)

enum class SecurityAndPrivacyHistoryVisibility {
    SinceSelection,
    SinceInvite,
    Anyone;

    /**
     * Returns the fallback visibility when the current visibility is not available.
     */
    fun fallback(): SecurityAndPrivacyHistoryVisibility {
        return when (this) {
            SinceSelection,
            SinceInvite -> SinceSelection
            Anyone -> SinceInvite
        }
    }
}

enum class SecurityAndPrivacyRoomAccess {
    InviteOnly,
    AskToJoin,
    Anyone,
    SpaceMember;

    fun canConfigureRoomVisibility(): Boolean {
        return when (this) {
            InviteOnly, SpaceMember -> false
            AskToJoin, Anyone -> true
        }
    }
}

sealed class SecurityAndPrivacyFailures : Exception() {
    data object SaveFailed : SecurityAndPrivacyFailures()
}
