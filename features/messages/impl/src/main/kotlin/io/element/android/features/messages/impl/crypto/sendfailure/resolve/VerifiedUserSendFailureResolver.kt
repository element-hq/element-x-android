/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import timber.log.Timber

class VerifiedUserSendFailureResolver(
    private val room: MatrixRoom,
    private val transactionId: TransactionId,
    private val iterator: VerifiedUserSendFailureIterator,
) {
    val currentSendFailure = mutableStateOf<LocalEventSendState.Failed.VerifiedUser?>(null)

    init {
        if (iterator.hasNext()) {
            currentSendFailure.value = iterator.next()
        }
    }

    suspend fun resend(): Result<Unit> {
        return room.retrySendMessage(transactionId)
            .onSuccess {
                Timber.d("Succeed to resend message with transactionId: $transactionId")
                currentSendFailure.value = null
            }
            .onFailure {
                Timber.e(it, "Failed to resend message with transactionId: $transactionId")
            }
    }

    suspend fun resolveAndResend(): Result<Unit> {
        return when (val failure = currentSendFailure.value) {
            is LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice -> {
                room.ignoreDeviceTrustAndResend(failure.devices, transactionId)
            }
            is LocalEventSendState.Failed.VerifiedUserChangedIdentity -> {
                room.withdrawVerificationAndResend(failure.users, transactionId)
            }
            else -> {
                Result.failure(IllegalStateException("Unknown send failure type"))
            }
        }.onSuccess {
            Timber.d("Succeed to resolve and resend message with transactionId: $transactionId")
            if (iterator.hasNext()) {
                val failure = iterator.next()
                currentSendFailure.value = failure
            } else {
                currentSendFailure.value = null
                Timber.d("No more failure to resolve for transactionId: $transactionId")
            }
        }.onFailure {
            Timber.e(it, "Failed to resolve and resend message with transactionId: $transactionId")
        }
    }
}
