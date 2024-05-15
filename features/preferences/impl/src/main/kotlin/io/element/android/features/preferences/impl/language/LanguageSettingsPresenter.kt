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

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import io.element.android.libraries.architecture.Presenter
import org.xmlpull.v1.XmlPullParser
import java.util.Locale
import javax.inject.Inject

class LanguageSettingsPresenter @Inject constructor() : Presenter<LanguageSettingsState> {
    @Composable
    override fun present(): LanguageSettingsState {
        val context = LocalContext.current

        val supportedLocales by remember { mutableStateOf(parseLocaleConfig(context)) }
        var selectedLocale by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales()) }

        fun handleEvents(event: LanguageSettingsEvents) {
            when (event) {
                is LanguageSettingsEvents.SetLocale -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(event.locale))
                    selectedLocale = AppCompatDelegate.getApplicationLocales()
                }
                LanguageSettingsEvents.SetToDefault -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    selectedLocale = AppCompatDelegate.getApplicationLocales()
                }
            }
        }

        return LanguageSettingsState(
            supportedLocales = supportedLocales,
            selectedLocale = selectedLocale,
            eventSink = { handleEvents(it) }
        )
    }

    // Since there is no androidx compatibility version of LocaleConfig, manual parsing of the resource file is needed
    @SuppressLint("DiscouragedApi") // We don't have access to the R values of :app here, which is why Resources.getIdentifier() is used
    private fun parseLocaleConfig(context: Context): List<Locale> {
        val list = mutableListOf<Locale>()

        val resources = context.resources
        val xml = resources.getXml(resources.getIdentifier("locales_config", "xml", context.packageName))

        while (xml.eventType != XmlPullParser.END_DOCUMENT) {
            when (xml.eventType) {
                XmlPullParser.START_TAG -> {
                    if (xml.name == "locale") {
                        val tag = xml.getAttributeValue("http://schemas.android.com/apk/res/android", "name")
                        list.add(Locale.forLanguageTag(tag))
                    }
                }
            }
            xml.next()
        }
        xml.close()

        return list.toList()
    }
}
