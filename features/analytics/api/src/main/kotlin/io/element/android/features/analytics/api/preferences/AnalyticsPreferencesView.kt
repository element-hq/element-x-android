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

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

private const val LINK_TAG = "link"

@Composable
fun AnalyticsPreferencesView(
    state: AnalyticsPreferencesState,
    modifier: Modifier = Modifier,
    onOpenAnalyticsPolicy: (url: String) -> Unit,
) {
    fun onEnabledChanged(isEnabled: Boolean) {
        state.eventSink(AnalyticsOptInEvents.EnableAnalytics(isEnabled = isEnabled))
    }

    val firstPart = stringResource(id = CommonStrings.screen_analytics_settings_help_us_improve, state.applicationName)
    val secondPart = buildAnnotatedStringWithStyledPart(
        CommonStrings.screen_analytics_settings_read_terms,
        CommonStrings.screen_analytics_settings_read_terms_content_link,
        tagAndLink = LINK_TAG to state.policyUrl,
    )
    val subtitle = buildAnnotatedString {
        append(firstPart)
        append("\n\n")
        append(secondPart)
    }

    ListItem(
        headlineContent = {
            Text(stringResource(id = CommonStrings.screen_analytics_settings_share_data))
        },
        supportingContent = {
            ClickableText(
                text = subtitle,
                onClick = {
                    subtitle
                        .getStringAnnotations(LINK_TAG, it, it)
                        .firstOrNull()
                        ?.let { stringAnnotation ->
                            onOpenAnalyticsPolicy(stringAnnotation.item)
                        }
                }
            )
        },
        leadingContent = null,
        trailingContent = ListItemContent.Switch(checked = state.isEnabled, onChange = ::onEnabledChanged),
        modifier = modifier,
    )
}

@Preview
@Composable
internal fun AnalyticsPreferencesViewLightPreview(@PreviewParameter(AnalyticsPreferencesStateProvider::class) state: AnalyticsPreferencesState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun AnalyticsPreferencesViewDarkPreview(@PreviewParameter(AnalyticsPreferencesStateProvider::class) state: AnalyticsPreferencesState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AnalyticsPreferencesState) {
    AnalyticsPreferencesView(
        state = state,
        onOpenAnalyticsPolicy = {},
    )
}
