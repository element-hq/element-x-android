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

package io.element.android.libraries.designsystem.compound.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.compound.CompoundTheme
import io.element.android.libraries.designsystem.compound.LocalCompoundColors
import io.element.android.libraries.designsystem.theme.components.ElementButtonDefaults
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun CompoundButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ElementButtonDefaults.shape,
    colors: ButtonColors = CompoundButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ElementButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ElementButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Button(
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

object CompoundButtonDefaults {
    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = LocalCompoundColors.current.buttonDisabled,
        disabledContentColor = LocalCompoundColors.current.textDisabled,
    )
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
internal fun PreviewCompoundButtonLight() {
    CompoundTheme(darkTheme = false) {
        CompoundButton(onClick = { /*TODO*/ }) {
            Text("A button")
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
internal fun PreviewCompoundButtonDisabledLight() {
    CompoundTheme(darkTheme = false) {
        CompoundButton(onClick = { /*TODO*/ }, enabled = false) {
            Text("A button")
        }
    }
}

@Preview
@Composable
internal fun PreviewCompoundButtonDark() {
    CompoundTheme(darkTheme = true) {
        CompoundButton(onClick = { /*TODO*/ }) {
            Text("A button")
        }
    }
}

@Preview
@Composable
internal fun PreviewCompoundButtonDisabledDark() {
    CompoundTheme(darkTheme = true) {
        CompoundButton(onClick = { /*TODO*/ }, enabled = false) {
            Text("A button")
        }
    }
}
