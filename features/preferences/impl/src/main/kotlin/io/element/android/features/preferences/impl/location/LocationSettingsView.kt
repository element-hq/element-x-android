/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.text.stringWithLink
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.ListSupportingTextDefaults
import io.element.android.libraries.designsystem.theme.components.Slider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.math.roundToInt

@Composable
fun LocationSettingsView(
    state: LocationSettingsState,
    onBackClick: () -> Unit,
    onOpenAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_location_sharing),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) {
        LiveLocationUpdatesSection(
            value = state.liveLocationMinimumDistanceUpdate,
            onSaveValue = { value ->
                state.eventSink(LocationSettingsEvent.SetLiveLocationMinimumDistanceUpdate(value))
            },
            onOpenAppPermissionsClick = onOpenAppSettingsClick,
        )
    }
}

@Composable
private fun ColumnScope.LiveLocationUpdatesSection(
    value: Int,
    onSaveValue: (Int) -> Unit,
    onOpenAppPermissionsClick: () -> Unit,
) {
    ListSectionHeader(
        title = stringResource(R.string.screen_advanced_settings_live_location_section_title),
        description = {
            ListSupportingText(
                text = stringResource(R.string.screen_advanced_settings_live_location_section_description),
                contentPadding = ListSupportingTextDefaults.Padding.None,
            )
        },
        hasDivider = false,
    )
    var sliderValue by remember(value) { mutableIntStateOf(value) }
    Column(
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.screen_advanced_settings_live_location_update_distance,
                sliderValue,
                sliderValue,
            ),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
        val valueRange = 1f..100f
        val start = valueRange.start.toInt()
        val end = valueRange.endInclusive.toInt()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${start}m", color = ElementTheme.colors.textSecondary, style = ElementTheme.typography.fontBodyMdRegular)
            Slider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                value = sliderValue.toFloat(),
                onValueChange = { sliderValue = it.roundToInt() },
                onValueChangeFinish = {
                    onSaveValue(sliderValue)
                },
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = ElementTheme.colors.iconAccentPrimary,
                    activeTrackColor = ElementTheme.colors.iconAccentPrimary,
                    inactiveTrackColor = ElementTheme.colors.bgBadgeAccent,
                    inactiveTickColor = ElementTheme.colors.iconAccentPrimary,
                )
            )
            Text("${end}m", color = ElementTheme.colors.textSecondary, style = ElementTheme.typography.fontBodyMdRegular)
        }
    }
    val footerText = stringWithLink(
        textRes = R.string.screen_advanced_settings_live_location_section_footer,
        url = "",
        linkTextRes = R.string.screen_advanced_settings_live_location_section_footer_link,
        onLinkClick = { onOpenAppPermissionsClick() },
    )
    ListSupportingText(
        annotatedString = footerText,
        contentPadding = ListSupportingTextDefaults.Padding.Default,
    )
}

@Preview
@Composable
internal fun LocationSettingsViewPreview(@PreviewParameter(LocationSettingsStatePreviewParam::class) state: LocationSettingsState) {
    LocationSettingsView(
        state = state,
        onBackClick = { },
        onOpenAppSettingsClick = {}
    )
}
