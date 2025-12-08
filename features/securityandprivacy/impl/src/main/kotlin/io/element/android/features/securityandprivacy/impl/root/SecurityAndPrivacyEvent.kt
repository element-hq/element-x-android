/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

sealed interface SecurityAndPrivacyEvent {
    data object EditRoomAddress : SecurityAndPrivacyEvent
    data object Save : SecurityAndPrivacyEvent
    data object Exit : SecurityAndPrivacyEvent
    data object DismissExitConfirmation : SecurityAndPrivacyEvent
    data class ChangeRoomAccess(val roomAccess: SecurityAndPrivacyRoomAccess) : SecurityAndPrivacyEvent
    data object ToggleEncryptionState : SecurityAndPrivacyEvent
    data object CancelEnableEncryption : SecurityAndPrivacyEvent
    data object ConfirmEnableEncryption : SecurityAndPrivacyEvent
    data class ChangeHistoryVisibility(val historyVisibility: SecurityAndPrivacyHistoryVisibility) : SecurityAndPrivacyEvent
    data object ToggleRoomVisibility : SecurityAndPrivacyEvent
    data object DismissSaveError : SecurityAndPrivacyEvent
}
