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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.os.LocaleListCompat
import java.util.Locale

open class LanguageSettingsStateProvider : PreviewParameterProvider<LanguageSettingsState> {
    override val values: Sequence<LanguageSettingsState>
        get() = sequenceOf(
            aLanguageSettingsState(),
            aLanguageSettingsState(LocaleListCompat.forLanguageTags("en")),
            aLanguageSettingsState(LocaleListCompat.forLanguageTags("zh-TW"))
        )
}

fun aLanguageSettingsState(
    selectedLocale: LocaleListCompat = LocaleListCompat.getEmptyLocaleList(),
    eventSink: (LanguageSettingsEvents) -> Unit = {}
) = LanguageSettingsState(
    supportedLocales = listOf(
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("de"),
        Locale.forLanguageTag("fr"),
        Locale.forLanguageTag("zh-CN"),
        Locale.forLanguageTag("zh-TW")
    ),
    selectedLocale = selectedLocale,
    eventSink = eventSink
)
