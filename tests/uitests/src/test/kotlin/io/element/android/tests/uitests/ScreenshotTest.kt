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
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.theme.ElementTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.Locale

/**
 * BMA: Inspired from https://github.com/airbnb/Showkase/blob/master/showkase-screenshot-testing-paparazzi-sample/src/test/java/com/airbnb/android/showkase/screenshot/testing/paparazzi/sample/PaparazziSampleScreenshotTest.kt
 */

/*
 * Credit to Alex Vanyo for creating this sample in the Now In Android app by Google.
 * PR here - https://github.com/android/nowinandroid/pull/101. Modified the test from that PR to
 * my own needs for this sample.
 */
@RunWith(Parameterized::class)
class ScreenshotTest(
    private val componentTestPreview: TestPreview,
) {

    companion object {

        val previews = screensPreviews() + designPreviews()

        @JvmStatic
        @Parameters(name = "preview")
        fun previews(): List<TestPreview> {
            return previews
        }

        fun screensPreviews(): List<TestPreview> {
            val metadata = Showkase.getMetadata()
            val components = metadata.componentList.filterNot {
                it.componentKey.startsWith("io.element.android.libraries.designsystem")
            }.map(::ComponentTestPreview)
            val colors = metadata.colorList.map(::ColorTestPreview)
            val typography = metadata.typographyList.map(::TypographyTestPreview)

            return components + colors + typography
        }

        fun designPreviews(): List<TestPreview> {
            val metadata = Showkase.getMetadata()
            val components = metadata.componentList.filterNot {
                it.componentKey.startsWith("io.element.android.libraries.designsystem")
            }.map(::ComponentTestPreview)
            val colors = metadata.colorList.map(::ColorTestPreview)
            val typography = metadata.typographyList.map(::TypographyTestPreview)

            return components + colors + typography
        }
//
//        @JvmStatic
//        @Parameters(name = "ui_mode")
//        fun mode(): List<Boolean> = listOf(false, true)
    }

    @Before
    fun setup() {
        System.gc()
        val r = Runtime.getRuntime()
        println("maxMemory  : " + r.maxMemory())
        println("totalMemory: " + r.totalMemory())
    }

    @get:Rule
    val paparazzi = Paparazzi(
        theme = "Theme.AppCompat.Light.NoActionBar",
        maxPercentDifference = 0.01,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun preview_tests_screens_light(
    ) {
        paparazzi.screenshotTest(
            componentTestPreview = componentTestPreview,
            baseDeviceConfig = BaseDeviceConfig.NEXUS_5,
            fontScale = 1.0f,
            localeStr = "en",
            darkMode = false,
        )
    }

    @Test
    fun preview_tests_screens_dark(
    ) {
        paparazzi.screenshotTest(
            componentTestPreview = componentTestPreview,
            baseDeviceConfig = BaseDeviceConfig.NEXUS_5,
            fontScale = 1.0f,
            localeStr = "en",
            darkMode = true,
        )
    }

//    @Test
//    fun preview_tests_design_system(
//        @TestParameter(valuesProvider = DesignSystemPreviewProvider::class) componentTestPreview: TestPreview,
//        @TestParameter baseDeviceConfig: BaseDeviceConfig,
//        @TestParameter(value = ["1.0"/*, "1.5"*/]) fontScale: Float,
//        @TestParameter(value = ["en" /*"fr", "de", "ru"*/]) localeStr: String,
//        @TestParameter(value = ["false"]) darkMode: Boolean,
//    ) {
//        paparazzi.screenshotTest(
//            componentTestPreview = componentTestPreview,
//            baseDeviceConfig = baseDeviceConfig,
//            fontScale = fontScale,
//            localeStr = localeStr,
//            darkMode = darkMode
//        )
//    }
}

private fun Paparazzi.screenshotTest(
    componentTestPreview: TestPreview,
    baseDeviceConfig: BaseDeviceConfig,
    fontScale: Float,
    localeStr: String,
    darkMode: Boolean,
) {
    val needsScrolling = componentTestPreview.needsScroll
    val screenHeight = componentTestPreview.customHeightDp.takeIf { it != null }
//    unsafeUpdateConfig(
//        deviceConfig = baseDeviceConfig.deviceConfig.copy(
//            softButtons = false,
//            screenHeight = screenHeight ?: baseDeviceConfig.deviceConfig.screenHeight
//        ),
//        renderingMode = if (needsScrolling) SessionParams.RenderingMode.V_SCROLL else SessionParams.RenderingMode.SHRINK
//    )
    snapshot {
        ElementTheme {
                Box(
                    modifier = Modifier
//                        .background(MaterialTheme.colorScheme.background)
                        .sizeIn(minWidth = 1.dp, minHeight = 1.dp)
//                        .applyIf(needsScrolling, ifTrue = {
//                            heightIn(max = 1000.dp)
//                        })
                ) {
                    componentTestPreview.Content()
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
