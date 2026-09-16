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

private const val FIRST_STRONG_ISOLATE = 0x2068
private const val POP_DIRECTIONAL_ISOLATE = 0x2069

/** Wraps the string between Unicode isolate characters so its own direction cannot reorder the text around it. */
internal fun String.bidiIsolate(): String = Char(FIRST_STRONG_ISOLATE) + this + Char(POP_DIRECTIONAL_ISOLATE)

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
