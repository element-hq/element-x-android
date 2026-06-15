/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import io.element.android.libraries.matrix.api.user.UserStatus

sealed interface UserStatusEvent {
    /** User tapped the status row — open the picker bottom sheet. */
    data object Open : UserStatusEvent
    /** User dismissed the bottom sheet without selecting. */
    data object Dismiss : UserStatusEvent
    /** User selected a predefined or confirmed a custom status. */
    data class Set(val status: UserStatus) : UserStatusEvent
    /** User tapped "Custom…" in the bottom sheet. */
    data object OpenCustomInput : UserStatusEvent
    /** User tapped Cancel in the inline custom input row. */
    data object CancelCustomInput : UserStatusEvent
    /** User changed the emoji in the custom input row. */
    data class UpdateCustomEmoji(val emoji: String) : UserStatusEvent
    /** User tapped the clear action on an existing status. */
    data object Clear : UserStatusEvent
}
