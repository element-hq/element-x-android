/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.blockedusers

import io.element.android.libraries.matrix.api.core.UserId

sealed interface BlockedUsersEvents {
    data class Unblock(val userId: UserId) : BlockedUsersEvents
    data object ConfirmUnblock : BlockedUsersEvents
    data object Cancel : BlockedUsersEvents
}
