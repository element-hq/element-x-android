/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import android.graphics.Typeface
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import io.element.android.compound.theme.LinkColor

fun String.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    append(this@toAnnotatedString)
    val spannable = SpannedString.valueOf(this@toAnnotatedString)
    spannable.getSpans(0, spannable.length, Any::class.java).forEach { span ->
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
            }
            is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
        }
    }
}

/**
 * Convert a string to an [AnnotatedString] with styles applied.
 *
 * @param fullTextRes the string resource to use as the full text. Must contain a single %s
 * @param coloredTextRes the string resource to use as the colored part of the string
 * @param color the color to apply to the string
 * @param underline whether to underline the string
 * @param bold whether to bold the string
 * @param tagAndLink an optional pair of tag and link to add to the styled part of the string, as StringAnnotation
 */
@Composable
fun buildAnnotatedStringWithStyledPart(
    @StringRes fullTextRes: Int,
    @StringRes coloredTextRes: Int,
    color: Color = LinkColor,
    underline: Boolean = true,
    bold: Boolean = false,
    tagAndLink: Pair<String, String>? = null,
) = buildAnnotatedString {
    val coloredPart = stringResource(coloredTextRes)
    val fullText = stringResource(fullTextRes, coloredPart)
    val startIndex = fullText.indexOf(coloredPart)
    append(fullText)
    addStyle(
        style = SpanStyle(
            color = color,
            textDecoration = if (underline) TextDecoration.Underline else null,
            fontWeight = if (bold) FontWeight.Bold else null,
        ),
        start = startIndex,
        end = startIndex + coloredPart.length,
    )
    if (tagAndLink != null) {
        addStringAnnotation(
            tag = tagAndLink.first,
            annotation = tagAndLink.second,
            start = startIndex,
            end = startIndex + coloredPart.length
        )
    }
}

/**
 * Convert a string to an [AnnotatedString] with colored end period if present.
 */
fun withColoredPeriod(
    text: String,
) = buildAnnotatedString {
    append(text)
    if (text.endsWith(".")) {
        addStyle(
            style = SpanStyle(
                // Light.colorGreen700
                color = Color(0xff0bc491),
            ),
            start = text.length - 1,
            end = text.length,
        )
    }
}
