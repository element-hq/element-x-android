/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.theme.ElementTheme

/**
 * Generates 2 previews of the composable it is applied to: day and night mode.
 *
 * NB: Content should be wrapped into [ElementPreview] to apply proper theming.
 */
@Preview(name = "D")
@Preview(name = "N", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ElementPreviews(
    val group: String = "",
    val widthDp: Int = -1,
    val heightDp: Int = -1,
)

@Deprecated("Don't use anymore.")
@Composable
fun ElementPreviewDark(content: @Composable () -> Unit) {}

@Composable
@Suppress("ModifierMissing")
fun ElementPreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementTheme(darkTheme = darkTheme) {
        if (showBackground) {
            // If we have a proper contentColor applied we need a Surface instead of a Box
            Surface { content() }
        } else {
            content()
        }
    }
}
