/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailureFactory
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResolveVerifiedUserSendFailurePresenter @Inject constructor(
    private val room: MatrixRoom,
    private val verifiedUserSendFailureFactory: VerifiedUserSendFailureFactory,
) : Presenter<ResolveVerifiedUserSendFailureState> {
    @Composable
    override fun present(): ResolveVerifiedUserSendFailureState {
        var resolver by remember {
            mutableStateOf<VerifiedUserSendFailureResolver?>(null)
        }
        val verifiedUserSendFailure by produceState<VerifiedUserSendFailure>(VerifiedUserSendFailure.None, resolver?.currentSendFailure?.value) {
            val currentSendFailure = resolver?.currentSendFailure?.value
            value = verifiedUserSendFailureFactory.create(currentSendFailure)
        }

        val resolveAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val retryAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: ResolveVerifiedUserSendFailureEvents) {
            when (event) {
                is ResolveVerifiedUserSendFailureEvents.ComputeForMessage -> {
                    val sendState = event.messageEvent.localSendState as? LocalEventSendState.Failed.VerifiedUser
                    val transactionId = event.messageEvent.transactionId
                    resolver = if (sendState != null && transactionId != null) {
                        VerifiedUserSendFailureResolver(
                            room = room,
                            transactionId = transactionId,
                            iterator = VerifiedUserSendFailureIterator.from(sendState)
                        )
                    } else {
                        null
                    }
                }
                ResolveVerifiedUserSendFailureEvents.Dismiss -> {
                    resolver = null
                }
                ResolveVerifiedUserSendFailureEvents.Retry -> {
                    coroutineScope.launch {
                        resolver?.run {
                            runUpdatingState(retryAction) {
                                resend()
                            }
                        }
                    }
                }
                ResolveVerifiedUserSendFailureEvents.ResolveAndResend -> {
                    coroutineScope.launch {
                        resolver?.run {
                            runUpdatingState(resolveAction) {
                                resolveAndResend()
                            }
                        }
                    }
                }
            }
        }

        return ResolveVerifiedUserSendFailureState(
            verifiedUserSendFailure = verifiedUserSendFailure,
            resolveAction = resolveAction.value,
            retryAction = retryAction.value,
            eventSink = ::handleEvents
        )
    }
}
