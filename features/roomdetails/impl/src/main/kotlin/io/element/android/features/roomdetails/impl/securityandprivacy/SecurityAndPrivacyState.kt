/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

data class SecurityAndPrivacyState(
    // the settings that are currently applied on the room.
    val savedSettings: SecurityAndPrivacySettings,
    // the settings the user wants to apply.
    val editedSettings: SecurityAndPrivacySettings,
    val homeserverName: String,
    val showEncryptionConfirmation: Boolean,
    val saveAction: AsyncAction<Unit>,
    val eventSink: (SecurityAndPrivacyEvents) -> Unit
) {

    val canBeSaved = savedSettings != editedSettings


    val availableHistoryVisibilities = buildSet {
        add(SecurityAndPrivacyHistoryVisibility.SinceSelection)
        if (editedSettings.roomAccess == SecurityAndPrivacyRoomAccess.Anyone && !editedSettings.isEncrypted) {
            add(SecurityAndPrivacyHistoryVisibility.Anyone)
        } else {
            add(SecurityAndPrivacyHistoryVisibility.SinceInvite)
        }
    }
    val showRoomAccessSection: Boolean = true
    val showRoomVisibilitySections = editedSettings.roomAccess != SecurityAndPrivacyRoomAccess.InviteOnly
    val showHistoryVisibilitySection = editedSettings.historyVisibility != null
    val showEncryptionSection = true
}

data class SecurityAndPrivacySettings(
    val roomAccess: SecurityAndPrivacyRoomAccess,
    val isEncrypted: Boolean,
    val historyVisibility: SecurityAndPrivacyHistoryVisibility?,
    val addressName: String?,
    val isVisibleInRoomDirectory: AsyncData<Boolean>
)

enum class SecurityAndPrivacyHistoryVisibility {
    SinceSelection, SinceInvite, Anyone;

    /**
     * Returns the fallback visibility when the current visibility is not available.
     */
    fun fallback(): SecurityAndPrivacyHistoryVisibility {
        return when (this) {
            SinceSelection -> SinceSelection
            SinceInvite -> Anyone
            Anyone -> SinceInvite
        }
    }
}

enum class SecurityAndPrivacyRoomAccess {
    InviteOnly, AskToJoin, Anyone, SpaceMember
}
