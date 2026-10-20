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
import kotlinx.collections.immutable.toImmutableList

open class AccountSelectStatePreviewParam : PreviewParameterProvider<AccountSelectState> {
    override val values: Sequence<AccountSelectState>
        get() = sequenceOf(
            anAccountSelectState(),
            anAccountSelectState(accounts = aMatrixUserList()),
            anAccountSelectState(linkPillText = "#room:matrix.org", accounts = aMatrixUserList().take(2)),
            anAccountSelectState(linkPillText = "\uD83D\uDCAC > #room:matrix.org", accounts = aMatrixUserList()),
            anAccountSelectState(linkPillText = "@alice:matrix.org", accounts = aMatrixUserList()),
            anAccountSelectState(
                linkPillText = "#room:matrix.org",
                accounts = aMatrixUserList().take(3).mapIndexed { index, matrixUser ->
                    when (index) {
                        0 -> matrixUser.copy(displayedStatus = DisplayedStatus.UserSet(UserStatus("\uD83C\uDFD6\uFE0F", "On holiday")))
                        1 -> matrixUser.copy(displayedStatus = DisplayedStatus.InCall(callJoinedTs = null))
                        else -> matrixUser
                    }
                },
            ),
        )
}

private fun anAccountSelectState(
    linkPillText: String? = null,
    accounts: List<MatrixUser> = listOf(),
) = AccountSelectState(
    linkPillText = linkPillText,
    accounts = accounts.toImmutableList(),
)
