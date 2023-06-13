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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ElementOutlinedButtonDefaults.shape,
    colors: ButtonColors = ElementOutlinedButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ElementOutlinedButtonDefaults.buttonElevation(),
    border: BorderStroke? = ElementOutlinedButtonDefaults.border,
    contentPadding: PaddingValues = ElementOutlinedButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

object ElementOutlinedButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    val shape: Shape @Composable get() = ButtonDefaults.outlinedShape
    val border: BorderStroke @Composable get() = ButtonDefaults.outlinedButtonBorder
    @Composable
    fun buttonElevation(): ButtonElevation = ButtonDefaults.buttonElevation()

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors()


}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonsPreview() = ElementThemedPreview { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {}, enabled = true) {
            Text(text = "Click me! - Enabled")
        }
        OutlinedButton(onClick = {}, enabled = false) {
            Text(text = "Click me! - Disabled")
        }
    }
}
