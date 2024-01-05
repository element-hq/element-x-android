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
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.features.lockscreen.impl.R
import io.element.android.features.lockscreen.impl.components.PinEntryTextField
import io.element.android.features.lockscreen.impl.pin.model.PinDigit
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypad
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtom
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PinUnlockView(
    state: PinUnlockState,
    isInAppUnlock: Boolean,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> state.eventSink.invoke(PinUnlockEvents.OnUseBiometric)
            else -> Unit
        }
    }
    Surface(modifier) {
        PinUnlockPage(state = state, isInAppUnlock = isInAppUnlock)
        if (state.showSignOutPrompt) {
            SignOutPrompt(
                isCancellable = state.isSignOutPromptCancellable,
                onSignOut = { state.eventSink(PinUnlockEvents.SignOut) },
                onDismiss = { state.eventSink(PinUnlockEvents.ClearSignOutPrompt) },
            )
        }
        if (state.signOutAction is AsyncData.Loading) {
            ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        }
        if (state.showBiometricUnlockError) {
            ErrorDialog(
                content = state.biometricUnlockErrorMessage ?: "",
                onDismiss = { state.eventSink(PinUnlockEvents.ClearBiometricError) }
            )
        }
    }
}

@Composable
private fun PinUnlockPage(
    state: PinUnlockState,
    isInAppUnlock: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val commonModifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(all = 20.dp)

        val header = @Composable {
            PinUnlockHeader(
                state = state,
                isInAppUnlock = isInAppUnlock,
                modifier = Modifier.padding(top = 60.dp)
            )
        }
        val footer = @Composable {
            PinUnlockFooter(
                modifier = Modifier.padding(top = 24.dp),
                showBiometricUnlock = state.showBiometricUnlock,
                onUseBiometric = {
                    state.eventSink(PinUnlockEvents.OnUseBiometric)
                },
                onForgotPin = {
                    state.eventSink(PinUnlockEvents.OnForgetPin)
                },
            )
        }
        val content = @Composable { constraints: BoxWithConstraintsScope ->
            if (isInAppUnlock) {
                val pinEntry = state.pinEntry.dataOrNull()
                if (pinEntry != null) {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    PinEntryTextField(
                        pinEntry = pinEntry,
                        isSecured = true,
                        onValueChange = {
                            state.eventSink(PinUnlockEvents.OnPinEntryChanged(it))
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                    )
                }
            } else {
                PinKeypad(
                    onClick = {
                        state.eventSink(PinUnlockEvents.OnPinKeypadPressed(it))
                    },
                    maxWidth = constraints.maxWidth,
                    maxHeight = constraints.maxHeight,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
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

@Composable
private fun SignOutPrompt(
    isCancellable: Boolean,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isCancellable) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_app_lock_signout_alert_title),
            content = stringResource(id = R.string.screen_app_lock_signout_alert_message),
            onSubmitClicked = onSignOut,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    } else {
        ErrorDialog(
            title = stringResource(id = R.string.screen_app_lock_signout_alert_title),
            content = stringResource(id = R.string.screen_app_lock_signout_alert_message),
            onDismiss = onSignOut,
            modifier = modifier,
        )
    }
}

@Composable
private fun PinUnlockCompactView(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    Row(modifier = modifier) {
        Column(Modifier.weight(1f)) {
            header()
            Spacer(modifier = Modifier.height(24.dp))
            footer()
        }
        BoxWithConstraints(
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
private fun PinUnlockExpandedView(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        header()
        BoxWithConstraints(
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
    isInAppUnlock: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (isInAppUnlock) {
            RoundedIconAtom(imageVector = Icons.Filled.Lock)
        } else {
            Icon(
                modifier = Modifier
                    .size(32.dp),
                tint = ElementTheme.colors.iconPrimary,
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = CommonStrings.common_enter_your_pin),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        val remainingAttempts = state.remainingAttempts.dataOrNull()
        val subtitle = if (remainingAttempts != null) {
            if (state.showWrongPinTitle) {
                pluralStringResource(id = R.plurals.screen_app_lock_subtitle_wrong_pin, count = remainingAttempts, remainingAttempts)
            } else {
                pluralStringResource(id = R.plurals.screen_app_lock_subtitle, count = remainingAttempts, remainingAttempts)
            }
        } else {
            ""
        }
        val subtitleColor = if (state.showWrongPinTitle) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.secondary
        }
        Text(
            text = subtitle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = subtitleColor,
        )
        if (!isInAppUnlock && state.pinEntry is AsyncData.Success) {
            Spacer(Modifier.height(24.dp))
            PinDotsRow(state.pinEntry.data)
        }
    }
}

@Composable
private fun PinUnlockFooter(
    showBiometricUnlock: Boolean,
    onUseBiometric: () -> Unit,
    onForgotPin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        if (showBiometricUnlock) {
            TextButton(text = stringResource(id = R.string.screen_app_lock_use_biometric_android), onClick = onUseBiometric)
        }
        TextButton(text = stringResource(id = R.string.screen_app_lock_forgot_pin), onClick = onForgotPin)
    }
}

@Composable
@PreviewsDayNight
internal fun PinUnlockInAppViewPreview(@PreviewParameter(PinUnlockStateProvider::class) state: PinUnlockState) {
    ElementPreview {
        PinUnlockView(
            state = state,
            isInAppUnlock = true,
        )
    }
}

@Composable
@PreviewsDayNight
internal fun PinUnlockDefaultViewPreview(@PreviewParameter(PinUnlockStateProvider::class) state: PinUnlockState) {
    ElementPreview {
        PinUnlockView(
            state = state,
            isInAppUnlock = false,
        )
    }
}

