/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
fun annotatedTextWithBold(text: String, boldText: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        val start = text.indexOf(boldText)
        val end = start + boldText.length
        val textRange = 0..text.length
        if (start in textRange && end in textRange) {
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
        }
    }
}
