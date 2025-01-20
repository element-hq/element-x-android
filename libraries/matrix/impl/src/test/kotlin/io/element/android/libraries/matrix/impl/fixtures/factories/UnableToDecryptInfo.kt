/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.UnableToDecryptInfo
import uniffi.matrix_sdk_crypto.UtdCause

internal fun aRustUnableToDecryptInfo(
    eventId: String,
    timeToDecryptMs: ULong? = null,
    cause: UtdCause = UtdCause.UNKNOWN,
    eventLocalAgeMillis: Long = 0L,
    userTrustsOwnIdentity: Boolean = false,
    senderHomeserver: String = "",
    ownHomeserver: String = "",
): UnableToDecryptInfo {
    return UnableToDecryptInfo(
        eventId = eventId,
        timeToDecryptMs = timeToDecryptMs,
        cause = cause,
        eventLocalAgeMillis = eventLocalAgeMillis,
        userTrustsOwnIdentity = userTrustsOwnIdentity,
        senderHomeserver = senderHomeserver,
        ownHomeserver = ownHomeserver,
    )
}
