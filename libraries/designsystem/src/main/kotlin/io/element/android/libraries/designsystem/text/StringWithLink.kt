/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun stringWithLink(
    @StringRes textRes: Int,
    url: String,
    onLinkClick: (String) -> Unit,
    @StringRes linkTextRes: Int = CommonStrings.action_learn_more,
) = buildAnnotatedString {
    val learnMoreStr = stringResource(linkTextRes)
    val fullText = stringResource(textRes, learnMoreStr)
    append(fullText)
    val learnMoreStartIndex = fullText.lastIndexOf(learnMoreStr)
    addStyle(
        style = SpanStyle(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            color = ElementTheme.colors.textPrimary
        ),
        start = learnMoreStartIndex,
        end = learnMoreStartIndex + learnMoreStr.length,
    )
    addLink(
        url = LinkAnnotation.Url(
            url = url,
            linkInteractionListener = {
                onLinkClick(url)
            }
        ),
        start = learnMoreStartIndex,
        end = learnMoreStartIndex + learnMoreStr.length,
    )
}
