/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components.switchaccount

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal data class AccountSwitchSectionState(
    val otherAccounts: ImmutableList<OtherAccount>,
    val isExpanded: Boolean,
    val canAddAccount: Boolean,
)

internal class AccountSwitchSectionStatePreviewParam : PreviewParameterProvider<AccountSwitchSectionState> {
    override val values: Sequence<AccountSwitchSectionState>
        get() {
            val otherAccounts = aMatrixUserList().drop(1).take(3).map { anOtherAccount(it) }.toImmutableList()
            return sequenceOf(
                AccountSwitchSectionState(
                    otherAccounts = otherAccounts,
                    isExpanded = false,
                    canAddAccount = true,
                ),
                AccountSwitchSectionState(
                    otherAccounts = otherAccounts,
                    isExpanded = true,
                    canAddAccount = true,
                ),
                AccountSwitchSectionState(
                    otherAccounts = otherAccounts,
                    isExpanded = true,
                    canAddAccount = false,
                ),
                AccountSwitchSectionState(
                    otherAccounts = persistentListOf(),
                    isExpanded = false,
                    canAddAccount = true,
                ),
                AccountSwitchSectionState(
                    otherAccounts = aMatrixUserList().drop(1).take(3).zip(listOf(3, 0, 120)) { user, unreadCount ->
                        anOtherAccount(user = user, unreadCount = unreadCount)
                    }.toImmutableList(),
                    isExpanded = true,
                    canAddAccount = true,
                ),
            )
        }
}

fun anOtherAccount(
    user: MatrixUser = aMatrixUser(),
    unreadCount: Int = 0,
) = OtherAccount(
    user = user,
    unreadCount = unreadCount,
)
