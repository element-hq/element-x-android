/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.linknewdevice.impl.screens.number.model.Digit
import io.element.android.features.linknewdevice.impl.screens.number.model.Number
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.coroutines.delay

@Composable
fun NumberTextField(
    number: Number,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = LocalInspectionMode.current || interactionSource.collectIsFocusedAsState().value
    BasicTextField(
        modifier = modifier,
        value = number.toText(),
        onValueChange = {
            onValueChange(it)
        },
        interactionSource = interactionSource,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        ),
        decorationBox = {
            NumberRow(
                number = number,
                hasFocus = isFocused,
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberRow(
    number: Number,
    hasFocus: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val length = number.length()
        number.digits.forEachIndexed { index, digit ->
            DigitView(
                digit = digit,
                isCurrent = index == length,
                drawCursor = hasFocus,
            )
        }
    }
}

@Composable
private fun DigitView(
    digit: Digit,
    isCurrent: Boolean,
    drawCursor: Boolean,
) {
    val shape = RoundedCornerShape(4.dp)
    val appearanceModifier = when (digit) {
        Digit.Empty -> {
            val color = if (isCurrent) {
                ElementTheme.colors.textPrimary
            } else {
                ElementTheme.colors.borderInteractiveSecondary
            }
            Modifier.border(1.dp, color, shape)
        }
        is Digit.Filled -> {
            Modifier.background(ElementTheme.colors.bgActionSecondaryPressed, shape)
        }
    }
    Box(
        modifier = Modifier
            .size(42.dp, 56.dp)
            .then(appearanceModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (digit is Digit.Filled) {
            Text(
                text = digit.value.toString(),
                style = ElementTheme.typography.fontHeadingLgBold,
                color = ElementTheme.colors.textPrimary,
            )
        } else if (drawCursor && isCurrent) {
            // Draw a blinking cursor
            BlinkingCursor()
        }
    }
}

@Composable
private fun BlinkingCursor() {
    var isCursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(isCursorVisible) {
        delay(500)
        // Toggle cursor visibility
        isCursorVisible = !isCursorVisible
    }
    if (isCursorVisible) {
        Spacer(
            modifier = Modifier
                .size(2.dp, 24.dp)
                .offset(x = (-5).dp)
                .background(ElementTheme.colors.textPrimary, RoundedCornerShape(1.dp))
        )
    }
}

@PreviewsDayNight
@Composable
internal fun NumberTextFieldPreview() {
    ElementPreview {
        val number = Number.createEmpty(4).fillWith("12")
        NumberTextField(
            number = number,
            onValueChange = {},
            onDone = {},
        )
    }
}
