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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.lockscreen.impl.R
import io.element.android.features.lockscreen.impl.pin.model.PinDigit
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.numpad.PinKeypad
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.theme.ElementTheme

@Composable
fun PinUnlockView(
    state: PinUnlockState,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        BoxWithConstraints {
            val commonModifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(all = 20.dp)

            val header = @Composable {
                PinUnlockHeader(
                    state = state,
                    modifier = Modifier.padding(top = 60.dp, bottom = 12.dp)
                )
            }
            val footer = @Composable {
                PinUnlockFooter()
            }
            val content = @Composable {
                PinKeypad(
                    onClick = {
                        state.eventSink(PinUnlockEvents.OnPinKeypadPressed(it))
                    },
                    horizontalArrangement = spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                )
            }
            if (maxHeight < 600.dp) {
                PinUnlockCompactView(
                    header = header,
                    footer = footer,
                    content = content,
                    modifier = commonModifier,
                )
            } else {
                PinUnlockExpandedView(
                    header = header,
                    footer = footer,
                    content = content,
                    modifier = commonModifier,
                )
            }
        }
    }
}

@Composable
fun PinUnlockCompactView(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1f)) {
            header()
            Spacer(modifier = Modifier.height(24.dp))
            footer()
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
fun PinUnlockExpandedView(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        header()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 40.dp),
        ) {
            content()
        }
        footer()
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
private fun PinUnlockFooter(
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        TextButton(text = "Use biometric", onClick = { })
        TextButton(text = stringResource(id = R.string.screen_app_lock_forgot_pin), onClick = { })
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

