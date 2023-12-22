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

package io.element.android.features.lockscreen.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.element.android.features.lockscreen.impl.pin.model.PinDigit
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.pinDigitBg
import io.element.android.compound.theme.ElementTheme

@Composable
fun PinEntryTextField(
    pinEntry: PinEntry,
    isSecured: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        modifier = modifier,
        value = pinEntry.toText(),
        onValueChange = {
            onValueChange(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            PinEntryRow(pinEntry = pinEntry, isSecured = isSecured)
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PinEntryRow(
    pinEntry: PinEntry,
    isSecured: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (digit in pinEntry.digits) {
            PinDigitView(digit = digit, isSecured = isSecured)
        }
    }
}

@Composable
private fun PinDigitView(
    digit: PinDigit,
    isSecured: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val appearanceModifier = when (digit) {
        PinDigit.Empty -> {
            Modifier.border(1.dp, ElementTheme.colors.iconPrimary, shape)
        }
        is PinDigit.Filled -> {
            Modifier.background(ElementTheme.colors.pinDigitBg, shape)
        }
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .then(appearanceModifier),
        contentAlignment = Alignment.Center,

        ) {
        if (digit is PinDigit.Filled) {
            val text = if (isSecured) {
                "•"
            } else {
                digit.value.toString()
            }
            Text(
                text = text,
                style = ElementTheme.typography.fontHeadingMdBold
            )
        }

    }
}

@PreviewsDayNight
@Composable
internal fun PinEntryTextFieldPreview() {
    ElementPreview {
        val pinEntry = PinEntry.createEmpty(4).fillWith("12")
        Column {
            PinEntryTextField(
                pinEntry = pinEntry,
                isSecured = true,
                onValueChange = {},
            )
            Spacer(modifier = Modifier.size(16.dp))
            PinEntryTextField(
                pinEntry = pinEntry,
                isSecured = false,
                onValueChange = {},
            )
        }
    }
}
