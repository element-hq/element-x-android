/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.user.toMatrixUser
import io.element.android.libraries.matrix.ui.components.switchaccount.OtherAccount
import io.element.android.libraries.sessionstorage.api.SessionStore
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

    // The permalink cannot change, so these values are computed once.
    private val linkPillText = permalinkData.toPillText()
    private val isRoomLink = permalinkData is PermalinkData.RoomLink

    @Composable
    override fun present(): AccountSelectState {
        // Only used by this screen, the current account of the application is not changed.
        var currentSessionId by remember { mutableStateOf<SessionId?>(null) }
        var isAccountListExpanded by remember { mutableStateOf(true) }
        val accounts by produceState(persistentListOf()) {
            // Default to the account currently used by the application.
            currentSessionId = sessionStore.getLatestSession()?.userId?.let(::SessionId)
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

        val currentAccount by remember {
            derivedStateOf {
                accounts.find { it.userId == currentSessionId } ?: accounts.firstOrNull()
            }
        }
        val otherAccounts by remember {
            derivedStateOf {
                accounts
                    .filter { it.userId != currentAccount?.userId }
                    .map { OtherAccount(user = it, unreadCount = 0) }
                    .toImmutableList()
            }
        }

        fun handleEvent(event: AccountSelectEvent) {
            when (event) {
                AccountSelectEvent.ToggleAccountListExpanded -> isAccountListExpanded = !isAccountListExpanded
                is AccountSelectEvent.SelectAccount -> {
                    currentSessionId = event.sessionId
                }
            }
        }

        return AccountSelectState(
            linkPillText = linkPillText,
            isRoomLink = isRoomLink,
            currentAccount = currentAccount,
            otherAccounts = otherAccounts,
            isAccountListExpanded = isAccountListExpanded,
            eventSink = ::handleEvent,
        )
    }
}
