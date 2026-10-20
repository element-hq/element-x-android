/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.toMatrixUser
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@AssistedInject
class AccountSelectPresenter(
    @Assisted private val permalinkData: PermalinkData,
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
) : Presenter<AccountSelectState> {
    @AssistedFactory
    fun interface Factory {
        fun create(permalinkData: PermalinkData): AccountSelectPresenter
    }

    @Composable
    override fun present(): AccountSelectState {
        val accounts by produceState(persistentListOf()) {
            // Do not use sessionStore.sessionsFlow() to not make it change when an account is selected.
            val accountFlows = sessionStore.getAllSessions().map { sessionData ->
                val matrixUser = sessionData.toMatrixUser()
                // The status is not stored in the session store, observe it from the client, if it is in memory.
                matrixClientProvider.getOrNull(matrixUser.userId)
                    ?.userProfile
                    ?.map { profile ->
                        matrixUser.copy(
                            displayedStatus = profile.displayedStatus,
                            rawStatus = profile.rawStatus,
                        )
                    }
                    ?: flowOf(matrixUser)
            }
            if (accountFlows.isEmpty()) {
                value = persistentListOf()
            } else {
                combine(accountFlows) { it.toImmutableList() }
                    .distinctUntilChanged()
                    .collect { value = it }
            }
        }

        val linkPillText = remember {
            permalinkData.toPillText()
        }

        return AccountSelectState(
            linkPillText = linkPillText,
            accounts = accounts,
        )
    }
}
