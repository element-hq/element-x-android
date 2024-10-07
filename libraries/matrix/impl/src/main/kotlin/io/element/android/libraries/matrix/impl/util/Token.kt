/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.sessionstorage.api.SessionData
import org.matrix.rustcomponents.sdk.Session
import java.security.MessageDigest

private val sha256 by lazy { MessageDigest.getInstance("SHA-256") }

@OptIn(ExperimentalStdlibApi::class)
private fun anonymizeToken(token: String): String {
    return sha256.digest(token.toByteArray()).toHexString()
}

fun SessionData?.anonymizedTokens(): Pair<String?, String?> {
    if (this == null) return null to null
    return anonymizeToken(accessToken) to refreshToken?.let { anonymizeToken(it) }
}

fun Session?.anonymizedTokens(): Pair<String?, String?> {
    if (this == null) return null to null
    return anonymizeToken(accessToken) to refreshToken?.let { anonymizeToken(it) }
}
