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

package ui

import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.lifecycle.Lifecycle
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import io.element.android.compound.theme.ElementTheme
import org.junit.Rule
import java.util.Locale

/**
 * BMA: Inspired from https://github.com/airbnb/Showkase/blob/master/showkase-screenshot-testing-paparazzi-sample/src/test/java/com/airbnb/android/showkase/screenshot/testing/paparazzi/sample/PaparazziSampleScreenshotTest.kt
 *
 * Credit to Alex Vanyo for creating this sample in the Now In Android app by Google.
 * PR here - https://github.com/android/nowinandroid/pull/101. Modified the test from that PR to
 * my own needs for this sample.
 *
 * *Note*: keep the class name as short as possible to get shorter filename for generated screenshot.
 * Long name was ScreenshotTest.
 */
open class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        environment = detectEnvironment(),
        maxPercentDifference = 0.01,
        renderingMode = SessionParams.RenderingMode.NORMAL,
    )

    protected fun doTest(
        componentTestPreview: TestPreview,
        baseDeviceConfig: BaseDeviceConfig,
        fontScale: Float,
        localeStr: String,
    ) {
        val locale = localeStr.toLocale()
        Locale.setDefault(locale) // Needed for regional settings, as first day of week
        val densityScale = baseDeviceConfig.deviceConfig.density.dpiValue / 160f
        val customScreenHeight = componentTestPreview.customHeightDp()?.value?.let { it * densityScale }?.toInt()
        paparazzi.unsafeUpdateConfig(
            deviceConfig = baseDeviceConfig.deviceConfig.copy(
                softButtons = false,
                locale = localeStr,
                nightMode = componentTestPreview.isNightMode().let {
                    when (it) {
                        true -> NightMode.NIGHT
                        false -> NightMode.NOTNIGHT
                    }
                },
                screenHeight = customScreenHeight ?: baseDeviceConfig.deviceConfig.screenHeight,
            ),
        )
        paparazzi.snapshot {
            val lifecycleOwner = LocalLifecycleOwner.current
            CompositionLocalProvider(
                LocalInspectionMode provides true,
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = fontScale
                ),
                LocalConfiguration provides Configuration().apply {
                    setLocales(LocaleList(locale))
                    uiMode = when (componentTestPreview.isNightMode()) {
                        true -> Configuration.UI_MODE_NIGHT_YES
                        false -> Configuration.UI_MODE_NIGHT_NO
                    }
                },
                // Needed so that UI that uses it don't crash during screenshot tests
                LocalOnBackPressedDispatcherOwner provides object : OnBackPressedDispatcherOwner {
                    override val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle
                    override val onBackPressedDispatcher: OnBackPressedDispatcher get() = OnBackPressedDispatcher()
                }
            ) {
                ElementTheme {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        componentTestPreview.Content()
                    }
                }
            }
        }
    }
}

private fun String.toLocale(): Locale {
    return when (this) {
        "en" -> Locale.US
        "fr" -> Locale.FRANCE
        "de" -> Locale.GERMAN
        else -> Locale.Builder().setLanguage(this).build()
    }
}
