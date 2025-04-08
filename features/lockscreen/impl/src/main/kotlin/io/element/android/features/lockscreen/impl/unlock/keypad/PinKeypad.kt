/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock.keypad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.digit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val spaceBetweenPinKey = 16.dp
private val minSizePinKey = 16.dp
private val maxSizePinKey = 80.dp

@Composable
fun PinKeypad(
    onClick: (PinKeypadModel) -> Unit,
    maxWidth: Dp,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val pinKeyMaxWidth = ((maxWidth - 2 * spaceBetweenPinKey) / 3).coerceIn(minSizePinKey, maxSizePinKey)
    val pinKeyMaxHeight = ((maxHeight - 3 * spaceBetweenPinKey) / 4).coerceIn(minSizePinKey, maxSizePinKey)
    val pinKeySize = if (pinKeyMaxWidth < pinKeyMaxHeight) pinKeyMaxWidth else pinKeyMaxHeight

    val horizontalArrangement = spacedBy(spaceBetweenPinKey, Alignment.CenterHorizontally)
    val verticalArrangement = spacedBy(spaceBetweenPinKey, Alignment.CenterVertically)
    Column(
        modifier = modifier.onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp) {
                val digitChar = event.digit
                if (digitChar != null) {
                    onClick(PinKeypadModel.Number(digitChar))
                    true
                } else if (event.key == Key.Backspace) {
                    onClick(PinKeypadModel.Back)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        },
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        PinKeypadRow(
            pinKeySize = pinKeySize,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            models = persistentListOf(PinKeypadModel.Number('1'), PinKeypadModel.Number('2'), PinKeypadModel.Number('3')),
            onClick = onClick,
        )
        PinKeypadRow(
            pinKeySize = pinKeySize,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            models = persistentListOf(PinKeypadModel.Number('4'), PinKeypadModel.Number('5'), PinKeypadModel.Number('6')),
            onClick = onClick,
        )
        PinKeypadRow(
            pinKeySize = pinKeySize,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            models = persistentListOf(PinKeypadModel.Number('7'), PinKeypadModel.Number('8'), PinKeypadModel.Number('9')),
            onClick = onClick,
        )
        PinKeypadRow(
            pinKeySize = pinKeySize,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            models = persistentListOf(PinKeypadModel.Empty, PinKeypadModel.Number('0'), PinKeypadModel.Back),
            onClick = onClick,
        )
    }
}

@Composable
private fun PinKeypadRow(
    models: ImmutableList<PinKeypadModel>,
    onClick: (PinKeypadModel) -> Unit,
    pinKeySize: Dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
) {
    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val commonModifier = Modifier.size(pinKeySize)
        for (model in models) {
            when (model) {
                is PinKeypadModel.Empty -> {
                    Spacer(modifier = commonModifier)
                }
                is PinKeypadModel.Back -> {
                    PinKeypadBackButton(
                        modifier = commonModifier,
                        onClick = { onClick(model) },
                    )
                }
                is PinKeypadModel.Number -> {
                    PinKeypadDigitButton(
                        size = pinKeySize,
                        modifier = commonModifier,
                        digit = model.number.toString(),
                        onClick = { onClick(model) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeypadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(color = ElementTheme.colors.bgSubtlePrimary)
            .clickable(onClick = onClick),
        content = content
    )
}

@Composable
private fun PinKeypadDigitButton(
    digit: String,
    size: Dp,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PinKeypadButton(
        modifier = modifier,
        onClick = { onClick(digit) }
    ) {
        val fontSize = size.toSp() / 2
        val originalFont = ElementTheme.typography.fontHeadingXlBold
        val ratio = fontSize.value / originalFont.fontSize.value
        val lineHeight = originalFont.lineHeight * ratio
        Text(
            text = digit,
            color = ElementTheme.colors.textPrimary,
            style = originalFont.copy(fontSize = fontSize, lineHeight = lineHeight, letterSpacing = 0.sp),
        )
    }
}

@Composable
private fun PinKeypadBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PinKeypadButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = stringResource(CommonStrings.a11y_delete),
        )
    }
}

@Composable
@PreviewsDayNight
internal fun PinKeypadPreview() {
    ElementPreview {
        BoxWithConstraints {
            PinKeypad(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                onClick = {}
            )
        }
    }
}
