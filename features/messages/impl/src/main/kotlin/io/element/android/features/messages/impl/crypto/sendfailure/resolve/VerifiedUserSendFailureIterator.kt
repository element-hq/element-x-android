/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import timber.log.Timber

/**
 * Iterator for [LocalEventSendState.Failed.VerifiedUser]
 * Allow to iterate through the internal state of the failure.
 * This is useful to allow solving the failure step by step (e.g. for each user).
 */
interface VerifiedUserSendFailureIterator : Iterator<LocalEventSendState.Failed.VerifiedUser> {
    companion object {
        fun from(failure: LocalEventSendState.Failed.VerifiedUser): VerifiedUserSendFailureIterator {
            return when (failure) {
                is LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice -> UnsignedDeviceSendFailureIterator(failure)
                is LocalEventSendState.Failed.VerifiedUserChangedIdentity -> ChangedIdentitySendFailureIterator(failure)
            }
        }
    }
}

class UnsignedDeviceSendFailureIterator(
    failure: LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice
) : VerifiedUserSendFailureIterator {
    private val iterator = failure.devices.iterator()

    init {
        if (!hasNext()) {
            Timber.w("Got $failure without any devices, shouldn't happen.")
        }
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): LocalEventSendState.Failed.VerifiedUser {
        val (userId, deviceIds) = iterator.next()
        return LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice(
            mapOf(userId to deviceIds)
        )
    }
}

class ChangedIdentitySendFailureIterator(
    failure: LocalEventSendState.Failed.VerifiedUserChangedIdentity
) : VerifiedUserSendFailureIterator {
    private val iterator = failure.users.iterator()

    init {
        if (!hasNext()) {
            Timber.w("Got $failure without any users, shouldn't happen.")
        }
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): LocalEventSendState.Failed.VerifiedUser {
        val userId = iterator.next()
        return LocalEventSendState.Failed.VerifiedUserChangedIdentity(
            listOf(userId)
        )
    }
}
