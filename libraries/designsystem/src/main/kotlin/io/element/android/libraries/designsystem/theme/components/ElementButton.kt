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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.color.elementContentColorFor
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme

@Composable
fun ElementButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = ElementTheme.colors.primary,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = elementContentColorFor(backgroundColor = containerColor),
            disabledContainerColor = containerColor
                .copy(alpha = 0.12f)
                .compositeOver(containerColor),
            disabledContentColor = elementContentColorFor(backgroundColor = containerColor)
                .copy(alpha = ContentAlpha.disabled)
        ),
        // TODO shape = ButtonShape,
        // TODO elevation = ButtonDefaults.elevation(
        //     defaultElevation = ElementTheme.elevation.default,
        //     pressedElevation = ElementTheme.elevation.pressed
        //     /* disabledElevation = 0.dp */
        // ),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = {
            ProvideTextStyle(
                value = ElementTheme.typography.body1
            ) {
                content()
            }
        }
    )
}

@Preview
@Composable
fun ElementButtonsLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun ElementButtonsDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        ElementButton(onClick = {}, enabled = true) {
            Text(text = "Click me! - Enabled")
        }
        ElementButton(onClick = {}, enabled = false) {
            Text(text = "Click me! - Disabled")
        }
    }
}
