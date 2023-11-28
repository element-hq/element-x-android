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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.SnackBarLabelColorDark
import io.element.android.compound.theme.SnackBarLabelColorLight
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.ButtonVisuals
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun Snackbar(
    message: String,
    modifier: Modifier = Modifier,
    action: ButtonVisuals? = null,
    dismissAction: ButtonVisuals? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = ElementTheme.materialColors.inverseOnSurface,
    actionContentColor: Color = actionContentColor(),
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    Snackbar(
        modifier = modifier,
        action = action?.let { @Composable { it.Composable() } },
        dismissAction = dismissAction?.let { @Composable { it.Composable() } },
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = { Text(text = message) },
    )
}

@Composable
fun Snackbar(
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    dismissAction: @Composable (() -> Unit)? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = ElementTheme.materialColors.inverseOnSurface,
    actionContentColor: Color = actionContentColor(),
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Snackbar(
        modifier = modifier,
        action = action,
        dismissAction = dismissAction,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = content,
    )
}

// TODO this color is temporary, an `inverse` version should be added to the semantic colors instead
@Composable
private fun actionContentColor(): Color {
    return if (ElementTheme.isLightTheme) {
        SnackBarLabelColorLight
    } else {
        SnackBarLabelColorDark
    }
}

@Preview(name = "Snackbar", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarPreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text")
    }
}

@Preview(name = "Snackbar with action", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionPreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text", action = ButtonVisuals.Text("Action", {}))
    }
}

@Preview(name = "Snackbar with action and close button", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionAndCloseButtonPreview() {
    ElementThemedPreview {
        Snackbar(
            message = "Snackbar supporting text",
            action = ButtonVisuals.Text("Action") {},
            dismissAction = ButtonVisuals.Icon(
                IconSource.Vector(CompoundIcons.Close)
            ) {}
        )
    }
}

@Preview(name = "Snackbar with action on new line", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionOnNewLinePreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text", action = ButtonVisuals.Text("Action", {}), actionOnNewLine = true)
    }
}

@Preview(name = "Snackbar with action and close button on new line", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionOnNewLineAndCloseButtonPreview() {
    ElementThemedPreview {
        Snackbar(
            message = "Snackbar supporting text",
            action = ButtonVisuals.Text("Action", {}),
            dismissAction = ButtonVisuals.Icon(
                IconSource.Vector(CompoundIcons.Close)
            ) {},
            actionOnNewLine = true
        )
    }
}
