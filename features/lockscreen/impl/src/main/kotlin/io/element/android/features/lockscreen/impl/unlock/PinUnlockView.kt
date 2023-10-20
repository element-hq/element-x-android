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

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.lockscreen.impl.pin.model.PinDigit
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.numpad.PinKeypad
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
fun PinUnlockView(
    state: PinUnlockState,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        HeaderFooterPage(
            header = {
                PinUnlockHeader(
                    state = state,
                    modifier = Modifier.padding(top = 60.dp, bottom = 12.dp)
                )
            },
            content = {
                Box(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PinKeypad(
                        onClick = {
                            state.eventSink(PinUnlockEvents.OnPinKeypadPressed(it))
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun PinDotsRow(
    pinEntry: PinEntry,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        for (digit in pinEntry.digits) {
            PinDot(isFilled = digit is PinDigit.Filled)
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isFilled) {
        ElementTheme.colors.iconPrimary
    } else {
        ElementTheme.colors.bgSubtlePrimary
    }
    Box(
        modifier = modifier
            .size(14.dp)
            .background(backgroundColor, CircleShape)
    )
}

@Composable
private fun PinUnlockHeader(
    state: PinUnlockState,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            modifier = Modifier
                .size(32.dp),
            tint = ElementTheme.colors.iconPrimary,
            imageVector = Icons.Filled.Lock,
            contentDescription = "",
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enter your PIN",
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You have 3 attempts to unlock",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(24.dp))
        PinDotsRow(state.pinEntry)
    }
}

@Composable
@PreviewsDayNight
internal fun PinUnlockViewPreview(@PreviewParameter(PinUnlockStateProvider::class) state: PinUnlockState) {
    ElementPreview {
        PinUnlockView(
            state = state,
        )
    }
}

