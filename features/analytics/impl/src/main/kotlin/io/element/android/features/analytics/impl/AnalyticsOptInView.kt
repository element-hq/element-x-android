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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
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
    onClickTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions(tag = "Analytics", msg = "Root")
    val eventSink = state.eventSink
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        header = { AnalyticsOptInHeader(state, onClickTerms) },
        content = { AnalyticsOptInContent() },
        footer = { AnalyticsOptInFooter(eventSink) })
}

@Composable
private fun AnalyticsOptInHeader(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 60.dp, bottom = 12.dp),
            title = stringResource(id = R.string.screen_analytics_prompt_title, state.applicationName),
            subTitle = stringResource(id = R.string.screen_analytics_prompt_help_us_improve),
            iconImageVector = Icons.Filled.Poll
        )
        Text(
            text = buildAnnotatedStringWithStyledPart(
                R.string.screen_analytics_prompt_read_terms,
                R.string.screen_analytics_prompt_read_terms_content_link,
                color = Color.Unspecified,
                underline = false,
                bold = true,
            ),
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .clickable { onClickTerms() }
                .padding(8.dp),
            style = ElementTextStyles.Regular.subheadline,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun AnalyticsOptInContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
private fun AnalyticsOptInContentRow(
    text: String,
    idx: Int,
    modifier: Modifier = Modifier,
) {
    val radius = 14.dp
    val bgShape = when (idx) {
        0 -> RoundedCornerShape(topStart = radius, topEnd = radius)
        2 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
    Row(
        modifier = modifier
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
private fun AnalyticsOptInFooter(
    eventSink: (AnalyticsOptInEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    ButtonColumnMolecule(
        modifier = modifier,
    ) {
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
    AnalyticsOptInView(
        state = state,
        onClickTerms = {},
    )
}
