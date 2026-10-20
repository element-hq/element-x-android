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
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.WarmUpRule
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
            assertThat(initialState.accounts).isEmpty()
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
            assertThat(initialState.accounts).hasSize(2)
            val firstAccount = initialState.accounts[0]
            assertThat(firstAccount).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID,
                    displayName = null,
                    avatarUrl = null,
                )
            )
            val secondAccount = initialState.accounts[1]
            assertThat(secondAccount).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID_2,
                    displayName = "Bob",
                    avatarUrl = "avatarUrl",
                    avatarThumbnail = "avatarData",
                )
            )
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
            val accounts = awaitItem().accounts
            assertThat(accounts[0].displayedStatus).isEqualTo(DisplayedStatus.UserSet(userStatus))
            assertThat(accounts[0].rawStatus).isEqualTo(userStatus)
            assertThat(accounts[1].displayedStatus).isNull()
            assertThat(accounts[1].rawStatus).isNull()
            // The status is updated live
            matrixClient.clearUserStatus()
            val updatedAccounts = awaitItem().accounts
            assertThat(updatedAccounts[0].displayedStatus).isNull()
            assertThat(updatedAccounts[0].rawStatus).isNull()
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
