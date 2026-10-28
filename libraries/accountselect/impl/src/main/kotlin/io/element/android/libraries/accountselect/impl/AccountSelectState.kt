/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.switchaccount.OtherAccount
import kotlinx.collections.immutable.ImmutableList

data class AccountSelectState(
    /**
     * The text of the pill representing the link to open, or null if the link must not be rendered.
     */
    val linkPillText: String?,
    /**
     * True if the link to open is a room or a message link.
     */
    val isRoomLink: Boolean,
    /**
     * The account which will be used to open the link, or null until the accounts are loaded.
     * Changing it only impacts this screen, not the current account of the application.
     */
    val currentAccount: MatrixUser?,
    val otherAccounts: ImmutableList<OtherAccount>,
    val isAccountListExpanded: Boolean,
    val eventSink: (AccountSelectEvent) -> Unit,
)
