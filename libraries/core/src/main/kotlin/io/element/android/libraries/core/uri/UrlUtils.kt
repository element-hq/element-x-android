/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.uri

import java.net.URI

fun String.isValidUrl(): Boolean {
    return try {
        URI(this).toURL()
        true
    } catch (t: Throwable) {
        false
    }
}

/**
 * Ensure string starts with "http". If it is not the case, "https://" is added, only if the String is not empty
 */
fun String.ensureProtocol(): String {
    return when {
        isEmpty() -> this
        !startsWith("http") -> "https://$this"
        else -> this
    }
}

fun String.ensureTrailingSlash(): String {
    return when {
        isEmpty() -> this
        !endsWith("/") -> "$this/"
        else -> this
    }
}
