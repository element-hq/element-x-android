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

package io.element.android.services.analytics.impl.settings

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.services.analytics.impl.R
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun AnalyticsOptInView(
    state: AnalyticsOptInState,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = { },
) {
    LogCompositions(tag = "Analytics", msg = "Root")
    val eventSink = state.eventSink
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState,
                )
                .padding(horizontal = 16.dp),
        ) {
            Image(
                painterResource(id = R.drawable.element_logo_stars),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
            Text(

                text = stringResource(id = R.string.screen_analytics_prompt_title, state.applicationName),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(id = R.string.screen_analytics_prompt_help_us_improve, state.applicationName),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
            )

            Text(
                text = buildAnnotatedStringWithColoredPart(
                    R.string.screen_analytics_prompt_read_terms,
                    R.string.screen_analytics_prompt_read_terms_content_link
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
            )

            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(id = R.drawable.ic_list_item_bullet),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(id = R.string.screen_analytics_prompt_data_usage).toAnnotatedString(),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(id = R.drawable.ic_list_item_bullet),
                    contentDescription = null,
                    Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(id = R.string.screen_analytics_prompt_third_party_sharing).toAnnotatedString(),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(id = R.drawable.ic_list_item_bullet),
                    contentDescription = null,
                    Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(id = R.string.screen_analytics_prompt_settings),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { eventSink(AnalyticsOptInEvents.EnableAnalytics(true)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = StringR.string.action_enable))
                }
                    Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { eventSink(AnalyticsOptInEvents.EnableAnalytics(false)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = StringR.string.action_not_now))
                }
                Spacer(Modifier.height(40.dp))
            }
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
            textDecoration = if(underline) TextDecoration.Underline else null
        ), start = startIndex, end = startIndex + coloredPart.length
    )
}

@Preview
@Composable
fun AnalyticsOptInViewLightPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun AnalyticsOptInViewDarkPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AnalyticsOptInState) {
    AnalyticsOptInView(state = state)
}
