/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailureFactory
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ResolveVerifiedUserSendFailurePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createResolveVerifiedUserSendFailurePresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
        }
    }

    @Test
    fun `present - remote message scenario`() = runTest {
        val presenter = createResolveVerifiedUserSendFailurePresenter()
        presenter.test {
            val sentMessage = aMessageEvent()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(sentMessage))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - sent message scenario`() = runTest {
        val presenter = createResolveVerifiedUserSendFailurePresenter()
        presenter.test {
            val sentMessage = aMessageEvent(
                sendState = LocalEventSendState.Sent(AN_EVENT_ID)
            )
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(sentMessage))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - unknown failed message scenario`() = runTest {
        val presenter = createResolveVerifiedUserSendFailurePresenter()
        presenter.test {
            val failedMessage = aMessageEvent(
                sendState = LocalEventSendState.Failed.Unknown("")
            )
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user unsigned device failure dismiss scenario`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        )
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserHasUnsignedDeviceFailedMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromYou)
                state.eventSink(ResolveVerifiedUserSendFailureEvents.Dismiss)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user unsigned device failure retry scenario`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        )
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserHasUnsignedDeviceFailedMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromYou)
                state.eventSink(ResolveVerifiedUserSendFailureEvents.Retry)
            }
            awaitItem().also { state ->
                assertThat(state.retryAction).isEqualTo(AsyncAction.Loading)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
                assertThat(state.retryAction).isEqualTo(AsyncAction.Success(Unit))
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user unsigned device failure resolve and resend scenario`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        ),
            ignoreDeviceTrustAndResendResult = { _, _ ->
                Result.success(Unit)
            },
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserHasUnsignedDeviceFailedMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromYou)
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }
            awaitItem().also { state ->
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Loading)
            }
            // This should move to the next user
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromOther(A_USER_ID_2.value))
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Success(Unit))
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }

            skipItems(3)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user unsigned device failure resolve and resend scenario with error`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        ),
            ignoreDeviceTrustAndResendResult = { _, _ ->
                Result.failure(Exception())
            },
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserHasUnsignedDeviceFailedMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromYou)
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }
            awaitItem().also { state ->
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.UnsignedDevice.FromYou)
                assertThat(state.resolveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user changed identity failure retry scenario`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        )
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserChangedIdentityMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(A_USER_ID.value))
                state.eventSink(ResolveVerifiedUserSendFailureEvents.Retry)
            }
            awaitItem().also { state ->
                assertThat(state.retryAction).isEqualTo(AsyncAction.Loading)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
                assertThat(state.retryAction).isEqualTo(AsyncAction.Success(Unit))
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user changed identity failure resolve and resend scenario`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        ),
            withdrawVerificationAndResendResult = { _, _ ->
                Result.success(Unit)
            },
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserChangedIdentityMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(A_USER_ID.value))
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }
            awaitItem().also { state ->
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Loading)
            }
            // This should move to the next user
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(A_USER_ID_2.value))
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Success(Unit))
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }

            skipItems(3)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - verified user changed identity failure resolve and resend scenario with error`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            userDisplayNameResult = { userId ->
                Result.success(userId.value)
            },
        ),
            withdrawVerificationAndResendResult = { _, _ ->
                Result.failure(Exception())
            },
        )
        val presenter = createResolveVerifiedUserSendFailurePresenter(room)
        presenter.test {
            val failedMessage = aVerifiedUserChangedIdentityMessage()
            val initialState = awaitItem()
            assertThat(initialState.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.None)
            initialState.eventSink(ResolveVerifiedUserSendFailureEvents.ComputeForMessage(failedMessage))

            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(A_USER_ID.value))
                state.eventSink(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
            }
            awaitItem().also { state ->
                assertThat(state.resolveAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(A_USER_ID.value))
                assertThat(state.resolveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
            ensureAllEventsConsumed()
        }
    }

    private fun aVerifiedUserHasUnsignedDeviceFailedMessage(): TimelineItem.Event {
        return aMessageEvent(
            transactionId = A_TRANSACTION_ID,
            sendState = LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice(
                mapOf(
                    A_USER_ID to emptyList(),
                    A_USER_ID_2 to emptyList()
                )
            )
        )
    }

    private fun aVerifiedUserChangedIdentityMessage(): TimelineItem.Event {
        return aMessageEvent(
            transactionId = A_TRANSACTION_ID,
            sendState = LocalEventSendState.Failed.VerifiedUserChangedIdentity(
                listOf(A_USER_ID, A_USER_ID_2)
            )
        )
    }

    private fun createResolveVerifiedUserSendFailurePresenter(
        room: FakeJoinedRoom = FakeJoinedRoom(),
    ): ResolveVerifiedUserSendFailurePresenter {
        return ResolveVerifiedUserSendFailurePresenter(
            room = room,
            verifiedUserSendFailureFactory = VerifiedUserSendFailureFactory(room),
        )
    }
}
