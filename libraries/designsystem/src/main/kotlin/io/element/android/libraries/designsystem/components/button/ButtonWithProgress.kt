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

package io.element.android.libraries.designsystem.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ElementButtonDefaults
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * A component that will display a button with an indeterminate circular progressbar.
 * When [showProgress] is true:
 *  - A circular progressbar is displayed.
 *  - [text] is replaced by [progressText], if defined.
 *  - [onClick] gets disabled.
 */
@Composable
fun ButtonWithProgress(
    text: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    progressText: String? = text,
    enabled: Boolean = true,
    shape: Shape = ElementButtonDefaults.shape,
    colors: ButtonColors = ElementButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ElementButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ElementButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Button(
        onClick = {
            if (!showProgress) { onClick() }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier
                    .progressSemantics()
                    .size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            if (progressText != null) {
                Spacer(Modifier.width(10.dp))
                Text(progressText, style = ElementTextStyles.Button)
            }
        } else if (text != null) {
            Text(text, style = ElementTextStyles.Button)
        }
    }
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun ButtonWithProgressPreview() = ElementThemedPreview {
    ButtonWithProgress(
        text = "Button with progress",
        onClick = {},
        showProgress = true,
    )
}
