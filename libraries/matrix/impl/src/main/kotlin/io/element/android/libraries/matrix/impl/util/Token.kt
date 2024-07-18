/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
