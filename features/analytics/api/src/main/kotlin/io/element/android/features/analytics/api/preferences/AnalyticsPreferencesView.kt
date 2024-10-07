/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.api.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.features.analytics.api.R
import io.element.android.libraries.designsystem.components.LINK_TAG
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun AnalyticsPreferencesView(
    state: AnalyticsPreferencesState,
    modifier: Modifier = Modifier,
) {
    fun onEnabledChanged(isEnabled: Boolean) {
        state.eventSink(AnalyticsOptInEvents.EnableAnalytics(isEnabled = isEnabled))
    }

    val supportingText = stringResource(
        id = R.string.screen_analytics_settings_help_us_improve,
        state.applicationName
    )
    val linkText = buildAnnotatedStringWithStyledPart(
        R.string.screen_analytics_settings_read_terms,
        R.string.screen_analytics_settings_read_terms_content_link,
        tagAndLink = LINK_TAG to state.policyUrl,
    )
    Column(modifier) {
        ListItem(
            headlineContent = {
                Text(stringResource(id = R.string.screen_analytics_settings_share_data))
            },
            supportingContent = {
                Text(supportingText)
            },
            leadingContent = null,
            trailingContent = ListItemContent.Switch(
                checked = state.isEnabled,
            ),
            onClick = {
                onEnabledChanged(!state.isEnabled)
            }
        )
        ListSupportingText(annotatedString = linkText)
    }
}

@PreviewsDayNight
@Composable
internal fun AnalyticsPreferencesViewPreview(@PreviewParameter(AnalyticsPreferencesStateProvider::class) state: AnalyticsPreferencesState) =
    ElementPreview {
        AnalyticsPreferencesView(
            state = state,
        )
    }
