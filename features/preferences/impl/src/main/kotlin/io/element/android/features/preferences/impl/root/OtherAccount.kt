/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.libraries.matrix.api.user.MatrixUser

/**
 * A signed-in account which is not the current one.
 *
 * @param user the user of the account.
 * @param unreadCount the total number of unread notifications for this account.
 */
data class OtherAccount(
    val user: MatrixUser,
    val unreadCount: Int,
)
