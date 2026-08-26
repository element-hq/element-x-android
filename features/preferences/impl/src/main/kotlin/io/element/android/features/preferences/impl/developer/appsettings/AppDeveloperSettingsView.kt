/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesView
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceDropdown
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceTextField
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.featureflag.ui.FeatureListView
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppDeveloperSettingsView(
    state: AppDeveloperSettingsState,
    onOpenShowkase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Note: this is OK to hardcode strings in this debug screen.
        PreferenceCategory(
            title = "Feature flags",
            showTopDivider = false,
        ) {
            FeatureListContent(state)
        }
        ElementCallCategory(state = state)
        PreferenceCategory(title = "Room list") {
            PreferenceDropdown(
                title = "Unread activity",
                supportingText = "What an unread room shows in the room list",
                selectedOption = state.roomListActivityVisibility,
                options = RoomListActivityVisibilityItem.entries.toImmutableList(),
                onSelectOption = { visibility ->
                    state.eventSink(AppDeveloperSettingsEvent.SetRoomListActivityVisibility(visibility))
                }
            )
        }
        PreferenceCategory(title = "Rust SDK") {
            PreferenceDropdown(
                title = "Tracing log level",
                supportingText = "Requires app reboot",
                selectedOption = state.tracingLogLevel.dataOrNull(),
                options = LogLevelItem.entries.toImmutableList(),
                onSelectOption = { logLevel ->
                    state.eventSink(AppDeveloperSettingsEvent.SetTracingLogLevel(logLevel))
                }
            )
        }
        PreferenceCategory(title = "Enable trace logs per SDK feature") {
            Text(
                text = "Requires app reboot",
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            for (logPack in TraceLogPack.entries) {
                PreferenceSwitch(
                    title = logPack.title,
                    isChecked = state.tracingLogPacks.contains(logPack),
                    onCheckedChange = { isChecked -> state.eventSink(AppDeveloperSettingsEvent.ToggleTracingLogPack(logPack, isChecked)) }
                )
            }
        }
        PreferenceCategory(title = "Showkase") {
            ListItem(
                content = {
                    Text("Open Showkase browser")
                },
                onClick = onOpenShowkase
            )
        }
        RageshakePreferencesView(
            state = state.rageshakeState,
        )
        PreferenceCategory(title = "Crash") {
            ListItem(
                content = {
                    Text("Crash the app 💥")
                },
                onClick = { error("This crash is a test.") }
            )
        }
        GitCategory(
            gitBranch = state.gitBranch,
            gitSha = state.gitSha,
        )
    }
}

@Composable
private fun GitCategory(
    gitBranch: String,
    gitSha: String,
) {
    PreferenceCategory(title = "Git") {
        val toastMessage = stringResource(CommonStrings.common_copied_to_clipboard)
        val context = LocalContext.current
        ListItem(
            content = { Text("Git branch") },
            supportingContent = { Text(text = gitBranch) },
            onClick = {
                context.copyToClipboard(
                    text = gitBranch,
                    toastMessage = toastMessage,
                )
            }
        )
        ListItem(
            content = { Text("Git SHA") },
            supportingContent = { Text(text = gitSha) },
            onClick = {
                context.copyToClipboard(
                    text = gitSha,
                    toastMessage = toastMessage,
                )
            }
        )
    }
}

@Composable
private fun ElementCallCategory(
    state: AppDeveloperSettingsState,
) {
    PreferenceCategory(title = "Element Call") {
        val callUrlState = state.customElementCallBaseUrlState

        val supportingText = if (callUrlState.baseUrl.isNullOrEmpty()) {
            stringResource(R.string.screen_advanced_settings_element_call_base_url_description)
        } else {
            callUrlState.baseUrl
        }
        PreferenceTextField(
            headline = stringResource(R.string.screen_advanced_settings_element_call_base_url),
            value = callUrlState.baseUrl,
            placeholder = "https://.../room",
            supportingText = supportingText,
            validation = callUrlState.validator,
            onValidationErrorMessage = stringResource(R.string.screen_advanced_settings_element_call_base_url_validation_error),
            displayValue = { value -> !value.isNullOrEmpty() },
            keyboardOptions = KeyboardOptions.Default.copy(autoCorrectEnabled = false, keyboardType = KeyboardType.Uri),
            onChange = { state.eventSink(AppDeveloperSettingsEvent.SetCustomElementCallBaseUrl(it)) }
        )
    }
}

@Composable
private fun FeatureListContent(
    state: AppDeveloperSettingsState,
) {
    fun onFeatureEnabled(feature: FeatureUiModel, isEnabled: Boolean) {
        state.eventSink(AppDeveloperSettingsEvent.UpdateEnabledFeature(feature, isEnabled))
    }

    FeatureListView(
        features = state.features,
        onCheckedChange = ::onFeatureEnabled,
    )
}

@PreviewsDayNight
@Composable
internal fun AppDeveloperSettingsViewPreview(
    @PreviewParameter(AppDeveloperSettingsStatePreviewParam::class) state: AppDeveloperSettingsState
) = ElementPreview {
    AppDeveloperSettingsView(
        state = state,
        onOpenShowkase = {},
    )
}
