/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.text

import android.graphics.Typeface
import android.text.SpannableString
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
    val spannable = SpannableString(this@toAnnotatedString)
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
