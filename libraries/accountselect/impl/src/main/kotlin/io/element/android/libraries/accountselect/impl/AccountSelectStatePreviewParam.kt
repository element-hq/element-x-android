/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.matrix.ui.components.switchaccount.OtherAccount
import io.element.android.libraries.matrix.ui.components.switchaccount.anOtherAccount
import kotlinx.collections.immutable.toImmutableList

open class AccountSelectStatePreviewParam : PreviewParameterProvider<AccountSelectState> {
    override val values: Sequence<AccountSelectState>
        get() = sequenceOf(
            anAccountSelectState(),
            anAccountSelectState(
                currentAccount = aMatrixUserList().first(),
                otherAccounts = anOtherAccountList(),
            ),
            anAccountSelectState(
                linkPillText = "#room:matrix.org",
                currentAccount = aMatrixUserList().first(),
                otherAccounts = anOtherAccountList().take(1),
            ),
            anAccountSelectState(
                linkPillText = "\uD83D\uDCAC > #room:matrix.org",
                currentAccount = aMatrixUserList().first(),
                otherAccounts = anOtherAccountList(),
            ),
            anAccountSelectState(
                linkPillText = "@alice:matrix.org",
                currentAccount = aMatrixUserList().first(),
                otherAccounts = anOtherAccountList(),
            ),
            anAccountSelectState(
                linkPillText = "#room:matrix.org",
                currentAccount = aMatrixUserList().first().copy(
                    displayedStatus = DisplayedStatus.UserSet(UserStatus("\uD83C\uDFD6\uFE0F", "On holiday")),
                ),
                otherAccounts = listOf(
                    anOtherAccount(aMatrixUserList()[1].copy(displayedStatus = DisplayedStatus.InCall(callJoinedTs = null))),
                    anOtherAccount(aMatrixUserList()[2]),
                ),
            ),
            anAccountSelectState(
                linkPillText = "#room:matrix.org",
                currentAccount = aMatrixUserList().first(),
                otherAccounts = anOtherAccountList(),
                isAccountListExpanded = false,
            ),
        )
}

private fun anOtherAccountList() = aMatrixUserList().drop(1).map { anOtherAccount(it) }

internal fun anAccountSelectState(
    linkPillText: String? = null,
    currentAccount: MatrixUser? = null,
    otherAccounts: List<OtherAccount> = emptyList(),
    isAccountListExpanded: Boolean = true,
    eventSink: (AccountSelectEvent) -> Unit = {},
) = AccountSelectState(
    linkPillText = linkPillText,
    currentAccount = currentAccount,
    otherAccounts = otherAccounts.toImmutableList(),
    isAccountListExpanded = isAccountListExpanded,
    eventSink = eventSink,
)
