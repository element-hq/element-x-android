/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.preferences.impl.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.list.RadioButtonListItem
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings
import java.util.Locale

@Composable
fun LanguageSettingsView(
    state: LanguageSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_language_settings)
    ) {
        RadioButtonListItem(
            headline = stringResource(id = R.string.screen_language_settings_system_default),
            selected = state.selectedLocale.isEmpty,
            onSelected = { state.eventSink(LanguageSettingsEvents.SetToDefault) }
        )
        state.supportedLocales.forEach { locale ->
            RadioButtonListItem(
                headline = locale.displayName,
                selected = localeMatches(locale, state.selectedLocale.get(0)),
                onSelected = { state.eventSink(LanguageSettingsEvents.SetLocale(locale)) },
            )
        }
    }
}

fun localeMatches(supportedLocale: Locale, selectedLocale: Locale?): Boolean {
    if (selectedLocale == null) return false
    if (supportedLocale.language != selectedLocale.language) return false
    // Do not attempt to match the country (which can be set from the system settings) if the supported version doesn't define one
    if (supportedLocale.country.isEmpty()) return true
    return supportedLocale.country == selectedLocale.country
}

@PreviewsDayNight
@Composable
internal fun LanguageSettingsViewPreview(@PreviewParameter(LanguageSettingsStateProvider::class) state: LanguageSettingsState) =
    ElementPreview {
        LanguageSettingsView(state = state, onBackPressed = { })
    }
