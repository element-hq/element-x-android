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

package base

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.TestName
import com.android.resources.NightMode
import io.element.android.compound.theme.ElementTheme
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import java.util.Locale

object ScreenshotTest {
    val defaultDeviceConfig = BaseDeviceConfig.NEXUS_5.deviceConfig

    fun runTest(
        paparazzi: Paparazzi,
        preview: ComposablePreview<AndroidPreviewInfo>,
        localeStr: String,
    ) {
        val locale = localeStr.toLocale()
        // Needed for regional settings, as first day of week
        Locale.setDefault(locale)

        paparazzi.fixScreenshotName(preview, localeStr)
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = 1.0f,
                ),
                LocalConfiguration provides Configuration().apply {
                    setLocales(LocaleList(locale))
                    uiMode = preview.previewInfo.uiMode
                },
            ) {
                ElementTheme {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        preview()
                    }
                }
            }
        }
    }
}

private val testNameField = Paparazzi::class.java.getDeclaredField("testName").apply {
    isAccessible = true
}

private fun Paparazzi.fixScreenshotName(preview: ComposablePreview<AndroidPreviewInfo>, locale: String) {
    val id = listOf(createScreenshotIdFor(preview), locale)
        .filter { it.isNotEmpty() }
        .joinToString("_")
    val packageName = preview.declaringClass
        // Remove common prefix
        .replace("io.element.android.", "")
        .split(".")
        // Remove class name
        .dropLast(1)
        .joinToString(".")
    val testName = TestName(
        packageName = packageName,
        className = preview.methodName.replace("Preview", ""),
        methodName = id
    )
    testNameField.set(this, testName)
}

private fun String.toLocale(): Locale {
    return when (this) {
        "en" -> Locale.US
        "fr" -> Locale.FRANCE
        "de" -> Locale.GERMAN
        else -> Locale.Builder().setLanguage(this).build()
    }
}

fun createScreenshotIdFor(preview: ComposablePreview<AndroidPreviewInfo>) = buildList {
    // `name` here can be `Day`, `Night`, or nothing at all
    if (preview.previewInfo.name.isNotEmpty()) {
        add(preview.previewInfo.name)
    }
    if (preview.previewInfo.group.isNotEmpty()) {
        add(preview.previewInfo.group)
    }
    // If it's a day/night preview, we should add an index to be consistent even if there is only version of this composable
    val needsIndex = preview.previewInfo.name == "Day" || preview.previewInfo.name == "Night"
    if (preview.previewIndex != null || needsIndex) {
        add((preview.previewIndex ?: 0).toString())
    }
}.joinToString("_")

object PaparazziPreviewRule {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>, locale: String, deviceConfig: DeviceConfig = ScreenshotTest.defaultDeviceConfig): Paparazzi {
        val densityScale = deviceConfig.density.dpiValue / 160f
        val customScreenHeight = preview.previewInfo.heightDp.takeIf { it >= 0 }?.let { it * densityScale }?.toInt()
        return Paparazzi(
            deviceConfig = deviceConfig.copy(
                nightMode = when (preview.previewInfo.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    true -> NightMode.NIGHT
                    false -> NightMode.NOTNIGHT
                },
                locale = locale,
                softButtons = false,
                screenHeight = customScreenHeight ?: deviceConfig.screenHeight,
            ),
            maxPercentDifference = 0.01
        )
    }
}
