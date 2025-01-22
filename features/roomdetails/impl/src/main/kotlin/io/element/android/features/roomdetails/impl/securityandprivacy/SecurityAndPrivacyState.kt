/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import io.element.android.libraries.architecture.AsyncData
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

data class SecurityAndPrivacyState(
    val savedSettings: SecurityAndPrivacySettings,
    val currentSettings: SecurityAndPrivacySettings,
    val homeserverName: String,
    val showEncryptionConfirmation: Boolean,
    val eventSink: (SecurityAndPrivacyEvents) -> Unit
) {

    val canBeSaved = savedSettings != currentSettings

    val showRoomVisibilitySections = currentSettings.roomAccess != SecurityAndPrivacyRoomAccess.InviteOnly && currentSettings.historyVisibility.isPresent

    val availableHistoryVisibilities = buildSet {
        add(SecurityAndPrivacyHistoryVisibility.SinceSelection)
        if (currentSettings.roomAccess == SecurityAndPrivacyRoomAccess.Anyone && !currentSettings.isEncrypted) {
            add(SecurityAndPrivacyHistoryVisibility.Anyone)
        } else {
            add(SecurityAndPrivacyHistoryVisibility.SinceInvite)
        }
        if (savedSettings.historyVisibility.getOrNull() == SecurityAndPrivacyHistoryVisibility.SinceInvite) {
            add(SecurityAndPrivacyHistoryVisibility.SinceInvite)
        }
    }
    val showRoomHistoryVisibilitySection = availableHistoryVisibilities.isNotEmpty() && currentSettings.historyVisibility.isPresent
}

data class SecurityAndPrivacySettings(
    val roomAccess: SecurityAndPrivacyRoomAccess,
    val isEncrypted: Boolean,
    val historyVisibility: Optional<SecurityAndPrivacyHistoryVisibility>,
    val addressName: Optional<String>,
    val isVisibleInRoomDirectory: Optional<AsyncData<Boolean>>
)

enum class SecurityAndPrivacyHistoryVisibility {
    SinceSelection, SinceInvite, Anyone
}

enum class SecurityAndPrivacyRoomAccess {
    InviteOnly, AskToJoin, Anyone, SpaceMember
}
