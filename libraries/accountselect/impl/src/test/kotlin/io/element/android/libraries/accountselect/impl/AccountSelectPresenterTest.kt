/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.ui.components.switchaccount.OtherAccount
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountSelectPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAccountSelectPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.linkPillText).isEqualTo(A_ROOM_ALIAS.value)
            assertThat(initialState.currentAccount).isNull()
            assertThat(initialState.otherAccounts).isEmpty()
            assertThat(initialState.isAccountListExpanded).isTrue()
        }
    }

    @Test
    fun `present - user link is rendered as a pill`() = runTest {
        val presenter = createAccountSelectPresenter(
            permalinkData = PermalinkData.UserLink(A_USER_ID),
        )
        presenter.test {
            assertThat(awaitItem().linkPillText).isEqualTo(A_USER_ID.value)
        }
    }

    @Test
    fun `present - multiple accounts case`() = runTest {
        val presenter = createAccountSelectPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(
                        sessionId = A_SESSION_ID_2.value,
                        userDisplayName = "Bob",
                        userAvatarUrl = "avatarUrl",
                        userAvatarData = "avatarData",
                    ),
                )
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            // The latest session is the current account
            assertThat(initialState.currentAccount).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID,
                    displayName = null,
                    avatarUrl = null,
                )
            )
            assertThat(initialState.otherAccounts).containsExactly(
                OtherAccount(
                    user = MatrixUser(
                        userId = A_SESSION_ID_2,
                        displayName = "Bob",
                        avatarUrl = "avatarUrl",
                        avatarThumbnail = "avatarData",
                    ),
                    unreadCount = 0,
                )
            )
        }
    }

    @Test
    fun `present - toggle the account list`() = runTest {
        val presenter = createAccountSelectPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(sessionId = A_SESSION_ID_2.value),
                )
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isAccountListExpanded).isTrue()
            initialState.eventSink(AccountSelectEvent.ToggleAccountListExpanded)
            val collapsedState = awaitItem()
            assertThat(collapsedState.isAccountListExpanded).isFalse()
            collapsedState.eventSink(AccountSelectEvent.ToggleAccountListExpanded)
            assertThat(awaitItem().isAccountListExpanded).isTrue()
        }
    }

    @Test
    fun `present - select another account only changes the current account of this screen`() = runTest {
        val presenter = createAccountSelectPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(sessionId = A_SESSION_ID_2.value),
                ),
                // Ensure that the latest session of the application is not changed
                setLatestSessionResult = { error("Should not be called") },
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.currentAccount?.userId).isEqualTo(A_SESSION_ID)
            initialState.eventSink(AccountSelectEvent.SelectAccount(A_SESSION_ID_2))
            val finalState = consumeItemsUntilPredicate { it.currentAccount?.userId == A_SESSION_ID_2 }.last()
            assertThat(finalState.currentAccount?.userId).isEqualTo(A_SESSION_ID_2)
            assertThat(finalState.otherAccounts.map { it.user.userId }).containsExactly(A_SESSION_ID)
            // The list is still expanded
            assertThat(finalState.isAccountListExpanded).isTrue()
        }
    }

    @Test
    fun `present - the status of the accounts is observed from the clients in memory`() = runTest {
        val userStatus = UserStatus(emoji = "\uD83C\uDFD6\uFE0F", text = "On holiday")
        val matrixClient = FakeMatrixClient(sessionId = A_SESSION_ID).apply {
            setUserStatus(userStatus)
        }
        val presenter = createAccountSelectPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(sessionId = A_SESSION_ID_2.value),
                )
            ),
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                if (sessionId == A_SESSION_ID) Result.success(matrixClient) else Result.failure(AN_EXCEPTION)
            },
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.currentAccount?.displayedStatus).isEqualTo(DisplayedStatus.UserSet(userStatus))
            assertThat(state.currentAccount?.rawStatus).isEqualTo(userStatus)
            assertThat(state.otherAccounts[0].user.displayedStatus).isNull()
            assertThat(state.otherAccounts[0].user.rawStatus).isNull()
            // The status is updated live
            matrixClient.clearUserStatus()
            val updatedState = awaitItem()
            assertThat(updatedState.currentAccount?.displayedStatus).isNull()
            assertThat(updatedState.currentAccount?.rawStatus).isNull()
        }
    }
}

internal fun createAccountSelectPresenter(
    permalinkData: PermalinkData = PermalinkData.RoomLink(RoomIdOrAlias.Alias(A_ROOM_ALIAS)),
    sessionStore: SessionStore = InMemorySessionStore(),
    matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider { Result.failure(AN_EXCEPTION) },
) = AccountSelectPresenter(
    permalinkData = permalinkData,
    sessionStore = sessionStore,
    matrixClientProvider = matrixClientProvider,
)
