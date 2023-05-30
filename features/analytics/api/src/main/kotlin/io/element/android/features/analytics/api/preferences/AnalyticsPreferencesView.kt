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

package io.element.android.features.analytics.api.preferences

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun AnalyticsPreferencesView(
    state: AnalyticsPreferencesState,
    modifier: Modifier = Modifier,
) {
    fun onEnabledChanged(isEnabled: Boolean) {
        state.eventSink(AnalyticsOptInEvents.EnableAnalytics(isEnabled = isEnabled))
    }

    PreferenceCategory(title = stringResource(id = StringR.string.screen_analytics_settings_share_data)) {
        val firstPart = stringResource(id = StringR.string.screen_analytics_settings_help_us_improve, state.applicationName)
        val secondPart = buildAnnotatedStringWithColoredPart(
            StringR.string.screen_analytics_settings_read_terms,
            StringR.string.screen_analytics_settings_read_terms_content_link
        )
        val title =  "$firstPart\n\n$secondPart"

        PreferenceSwitch(
            title = title,
            isChecked = state.isEnabled,
            onCheckedChange = ::onEnabledChanged
        )
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
fun AnalyticsPreferencesViewLightPreview(@PreviewParameter(AnalyticsPreferencesStateProvider::class) state: AnalyticsPreferencesState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun AnalyticsPreferencesViewDarkPreview(@PreviewParameter(AnalyticsPreferencesStateProvider::class) state: AnalyticsPreferencesState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AnalyticsPreferencesState) {
    AnalyticsPreferencesView(state)
}
