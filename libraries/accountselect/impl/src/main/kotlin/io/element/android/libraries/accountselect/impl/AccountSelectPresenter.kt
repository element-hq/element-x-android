/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Inject
class AccountSelectPresenter(
    private val sessionStore: SessionStore,
) : Presenter<AccountSelectState> {
    @Composable
    override fun present(): AccountSelectState {
        val accounts by produceState(persistentListOf()) {
            // Do not use sessionStore.sessionsFlow() to not make it change when an account is selected.
            value = sessionStore.getAllSessions()
                .map {
                    MatrixUser(
                        userId = UserId(it.userId),
                        displayName = it.userDisplayName,
                        avatarUrl = it.userAvatarUrl,
                    )
                }
                .toPersistentList()
        }

        return AccountSelectState(
            accounts = accounts,
        )
    }
}
