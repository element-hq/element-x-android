/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toImmutableList

open class AccountSelectStateProvider : PreviewParameterProvider<AccountSelectState> {
    override val values: Sequence<AccountSelectState>
        get() = sequenceOf(
            anAccountSelectState(),
            anAccountSelectState(accounts = aMatrixUserList()),
        )
}

private fun anAccountSelectState(
    accounts: List<MatrixUser> = listOf(),
) = AccountSelectState(
    accounts = accounts.toImmutableList(),
)
