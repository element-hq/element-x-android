/*
 * Copyright 2022 The Android Open Source Project
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.tests.uitests

import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.Showkase
import com.android.ide.common.rendering.api.SessionParams
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.theme.ElementTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * BMA: Inspired from https://github.com/airbnb/Showkase/blob/master/showkase-screenshot-testing-paparazzi-sample/src/test/java/com/airbnb/android/showkase/screenshot/testing/paparazzi/sample/PaparazziSampleScreenshotTest.kt
 */

/*
 * Credit to Alex Vanyo for creating this sample in the Now In Android app by Google.
 * PR here - https://github.com/android/nowinandroid/pull/101. Modified the test from that PR to
 * my own needs for this sample.
 */
@RunWith(TestParameterInjector::class)
class ScreenshotTest {

    object PreviewProvider : TestParameter.TestParameterValuesProvider {
        override fun provideValues(): List<TestPreview> {
            val metadata = Showkase.getMetadata()
            val components = metadata.componentList.map(::ComponentTestPreview)
            val colors = metadata.colorList.map(::ColorTestPreview)
            val typography = metadata.typographyList.map(::TypographyTestPreview)

            return components + colors + typography
        }
    }

    @get:Rule
    val paparazzi = Paparazzi(
        maxPercentDifference = 0.01,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun preview_tests(
        @TestParameter(valuesProvider = PreviewProvider::class) componentTestPreview: TestPreview,
        @TestParameter baseDeviceConfig: BaseDeviceConfig,
        @TestParameter(value = ["1.0"/*, "1.5"*/]) fontScale: Float,
        @TestParameter(value = ["en" /*"fr", "de", "ru"*/]) localeStr: String,
        @TestParameter(value = ["false", "true"]) darkMode: Boolean,
    ) {
        val needsScrolling = componentTestPreview.needsScroll
        val screenHeight = componentTestPreview.customHeightDp.takeIf { it != null }
        paparazzi.unsafeUpdateConfig(
            deviceConfig = baseDeviceConfig.deviceConfig.copy(
                softButtons = false,
                screenHeight = screenHeight ?: baseDeviceConfig.deviceConfig.screenHeight
            ),
            renderingMode = if (needsScrolling) SessionParams.RenderingMode.V_SCROLL else SessionParams.RenderingMode.SHRINK
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
                    setLocales(LocaleList(localeStr.toLocale()))
                    if (darkMode) uiMode = Configuration.UI_MODE_NIGHT_YES
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
                            .sizeIn(minWidth = 1.dp, minHeight = 1.dp)
                            .applyIf(needsScrolling, ifTrue = {
                                heightIn(max = 1000.dp)
                            })
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
        "en" -> Locale.ENGLISH
        "fr" -> Locale.FRANCE
        "de" -> Locale.GERMAN
        else -> Locale.Builder().setLanguage(this).build()
    }
}
