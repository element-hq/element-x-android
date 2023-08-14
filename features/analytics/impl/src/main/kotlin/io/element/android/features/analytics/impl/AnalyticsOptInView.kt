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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.molecules.InfoListItem
import io.element.android.libraries.designsystem.atomic.molecules.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.temporaryColorBgSpecial
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AnalyticsOptInView(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions(tag = "Analytics", msg = "Root")
    val eventSink = state.eventSink

    fun onTermsAccepted() {
        eventSink(AnalyticsOptInEvents.EnableAnalytics(true))
    }

    fun onTermsDeclined() {
        eventSink(AnalyticsOptInEvents.EnableAnalytics(false))
    }

    BackHandler(onBack = ::onTermsDeclined)
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        header = { AnalyticsOptInHeader(state, onClickTerms) },
        content = { AnalyticsOptInContent() },
        footer = {
            AnalyticsOptInFooter(
                onTermsAccepted = ::onTermsAccepted,
                onTermsDeclined = ::onTermsDeclined,
            )
        }
    )
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
            style = ElementTheme.typography.fontBodyMdRegular,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun CheckIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .size(20.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
            .padding(2.dp),
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
        tint = ElementTheme.colors.textActionAccent,
    )
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
        InfoListOrganism(
            items = persistentListOf(
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_data_usage),
                    iconComposable = { CheckIcon() },
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_third_party_sharing),
                    iconComposable = { CheckIcon() },
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_settings),
                    iconComposable = { CheckIcon() },
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyMdMedium,
            iconTint = ElementTheme.colors.textPrimary,
            backgroundColor = ElementTheme.colors.temporaryColorBgSpecial
        )
    }
}

@Composable
private fun AnalyticsOptInFooter(
    onTermsAccepted: () -> Unit,
    onTermsDeclined: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ButtonColumnMolecule(
        modifier = modifier,
    ) {
        Button(
            text = stringResource(id = CommonStrings.action_ok),
            onClick = onTermsAccepted,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            text = stringResource(id = CommonStrings.action_not_now),
            size = ButtonSize.Medium,
            onClick = onTermsDeclined,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
internal fun AnalyticsOptInViewLightPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewLight {
    ContentToPreview(state)
}

@Preview
@Composable
internal fun AnalyticsOptInViewDarkPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreviewDark {
    ContentToPreview(state)
}

@Composable
private fun ContentToPreview(state: AnalyticsOptInState) {
    AnalyticsOptInView(
        state = state,
        onClickTerms = {},
    )
}
