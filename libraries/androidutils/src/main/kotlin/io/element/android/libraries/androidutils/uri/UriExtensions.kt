/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.uri

import android.net.Uri

const val IGNORED_SCHEMA = "ignored"

fun createIgnoredUri(path: String): Uri = Uri.parse("$IGNORED_SCHEMA://$path")

fun Uri.setQueryParameter(key: String, value: String): Uri {
    val existingParams = queryParameterNames
    return buildUpon().apply {
        clearQuery()
        existingParams.forEach { existingKey ->
            if (existingKey != key) {
                appendQueryParameter(existingKey, getQueryParameter(existingKey))
            }
        }
        appendQueryParameter(key, value)
    }.build()
}
