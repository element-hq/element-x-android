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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Surface

@Composable
@Deprecated(
    message = "ElementPreviewLight is deprecated. Use ElementPreview directly instead.",
    replaceWith = ReplaceWith("ElementPreview(darkTheme, showBackground, content)")
)
fun ElementPreviewLight(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        darkTheme = darkTheme,
        showBackground = showBackground,
        content = content
    )
}

@Composable
@Deprecated(
    message = "ElementPreviewDark is deprecated. Use ElementPreview directly instead.",
    replaceWith = ReplaceWith("ElementPreview(darkTheme, showBackground, content)")
)
fun ElementPreviewDark(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        darkTheme = darkTheme,
        showBackground = showBackground,
        content = content
    )
}

@Composable
@Suppress("ModifierMissing")
fun ElementThemedPreview(
    showBackground: Boolean = true,
    vertical: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Color.Gray)
            .padding(4.dp)
    ) {
        if (vertical) {
            Column {
                ElementPreview(
                    darkTheme = false,
                    showBackground = showBackground,
                    content = content
                )
                Spacer(modifier = Modifier.height(4.dp))
                ElementPreview(
                    darkTheme = true,
                    showBackground = showBackground,
                    content = content
                )
            }
        } else {
            Row {
                ElementPreview(
                    darkTheme = false,
                    showBackground = showBackground,
                    content = content,
                )
                Spacer(modifier = Modifier.width(4.dp))
                ElementPreview(
                    darkTheme = true,
                    showBackground = showBackground,
                    content = content
                )
            }
        }
    }
}

@Preview(name = "Day theme")
@Preview(name = "Night theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ElementPreviews

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

