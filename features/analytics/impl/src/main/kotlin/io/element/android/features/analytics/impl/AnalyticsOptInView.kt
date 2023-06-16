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

package io.element.android.features.analytics.impl

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun AnalyticsOptInView(
    state: AnalyticsOptInState,
    modifier: Modifier = Modifier,
) {
    LogCompositions(tag = "Analytics", msg = "Root")
    val eventSink = state.eventSink
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        header = { AnalyticsOptInHeader(state) },
        content = { AnalyticsOptInContent() },
        footer = { AnalyticsOptInFooter(eventSink) })
}

@Composable
fun AnalyticsOptInHeader(state: AnalyticsOptInState) {
    Column {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 60.dp),
            title = stringResource(id = R.string.screen_analytics_prompt_title, state.applicationName),
            subTitle = stringResource(id = R.string.screen_analytics_prompt_help_us_improve),
            iconImageVector = Icons.Filled.Poll
        )
        Text(
            text = buildAnnotatedStringWithColoredPart(
                R.string.screen_analytics_prompt_read_terms,
                R.string.screen_analytics_prompt_read_terms_content_link
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            style = ElementTextStyles.Regular.subheadline,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
fun AnalyticsOptInContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = -0.4f
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsOptInContentRow(
                text = stringResource(id = R.string.screen_analytics_prompt_data_usage),
                idx = 0
            )
            AnalyticsOptInContentRow(
                text = stringResource(id = R.string.screen_analytics_prompt_third_party_sharing),
                idx = 1
            )
            AnalyticsOptInContentRow(
                text = stringResource(id = R.string.screen_analytics_prompt_settings),
                idx = 2
            )
        }
    }
}

@Composable
fun AnalyticsOptInContentRow(
    text: String,
    idx: Int,
) {
    val radius = 14.dp
    val bgShape = when (idx) {
        0 -> RoundedCornerShape(topStart = radius, topEnd = radius)
        2 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LocalColors.current.quinary,
                shape = bgShape,
            )
            .padding(vertical = 12.dp, horizontal = 20.dp),
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
                .padding(2.dp),
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            // TODO Compound, this color is not yet in the theme
            tint = Color(0xFF007A61)
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun AnalyticsOptInFooter(eventSink: (AnalyticsOptInEvents) -> Unit) {
    ButtonColumnMolecule {
        Button(
            onClick = { eventSink(AnalyticsOptInEvents.EnableAnalytics(true)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = StringR.string.action_ok))
        }
        TextButton(
            onClick = { eventSink(AnalyticsOptInEvents.EnableAnalytics(false)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = StringR.string.action_not_now))
        }
    }
}

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

@Composable
fun buildAnnotatedStringWithColoredPart(
    @StringRes fullTextRes: Int,
    @StringRes coloredTextRes: Int,
    color: Color = LinkColor,
    underline: Boolean = true,
) = buildAnnotatedString {
    val coloredPart = stringResource(coloredTextRes)
    val fullText = stringResource(fullTextRes, coloredPart)
    val startIndex = fullText.indexOf(coloredPart)
    append(fullText)
    addStyle(
        style = SpanStyle(
            color = color,
            textDecoration = if (underline) TextDecoration.Underline else null
        ), start = startIndex, end = startIndex + coloredPart.length
    )
}

@Preview
@Composable
fun AnalyticsOptInViewLightPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewLight {
    ContentToPreview(state)
}

@Preview
@Composable
fun AnalyticsOptInViewDarkPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewDark {
    ContentToPreview(state)
}

@Composable
private fun ContentToPreview(state: AnalyticsOptInState) {
    AnalyticsOptInView(state = state)
}
