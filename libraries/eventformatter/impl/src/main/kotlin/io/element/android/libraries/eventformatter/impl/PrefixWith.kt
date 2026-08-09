/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.impl

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Wraps the string between Unicode isolate characters (FSI … PDI) so its own direction cannot
 * reorder the text around it.
 *
 * A room list preview is rendered as a single paragraph, so a right-to-left display name otherwise
 * flips the whole line — the message body ends up on the wrong side of the name and the trailing
 * colon jumps to the front. See https://github.com/element-hq/element-x-android/issues/3338
 */
internal fun String.bidiIsolate(): String = "⁨$this⁩"

internal fun CharSequence.prefixWith(prefix: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(prefix)
        }
        append(": ")
        if (this@prefixWith is AnnotatedString) {
            append(this@prefixWith)
        } else {
            append(this@prefixWith.toString())
        }
    }
}
