/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import io.element.android.libraries.matrix.api.user.UserStatus

sealed interface UserStatusEvent {
    data object OpenPicker : UserStatusEvent
    data object DismissPicker : UserStatusEvent
    data class SetStatus(val status: UserStatus) : UserStatusEvent
    data object OpenCustomInput : UserStatusEvent
    data object CancelCustomInput : UserStatusEvent
    data class UpdateCustomEmoji(val emoji: String) : UserStatusEvent
    data object ClearStatus : UserStatusEvent
}
