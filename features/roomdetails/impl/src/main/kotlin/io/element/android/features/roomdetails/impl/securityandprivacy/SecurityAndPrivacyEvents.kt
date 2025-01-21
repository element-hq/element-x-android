/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

sealed interface SecurityAndPrivacyEvents {
    data object Save : SecurityAndPrivacyEvents
    data class ChangeRoomAccess(val roomAccess: SecurityAndPrivacyRoomAccess) : SecurityAndPrivacyEvents
    data object EnableEncryption: SecurityAndPrivacyEvents
    data class ChangeHistoryVisibility(val historyVisibility: SecurityAndPrivacyHistoryVisibility) : SecurityAndPrivacyEvents
    data class ChangeRoomVisibility(val isVisibleInRoomDirectory: Boolean) : SecurityAndPrivacyEvents
}
