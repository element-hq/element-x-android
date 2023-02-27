/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.verifysession

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R.string as StringR

@Composable
fun VerifySelfSessionScreen(
    state: VerifySelfSessionState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
            HeaderContent(verificationState = state.state)
            Content(verificationState = state.state, modifier = Modifier.weight(1f))
        }
        BottomMenu(screenState = state)
    }
}

@Composable
internal fun HeaderContent(verificationState: VerificationState, modifier: Modifier = Modifier) {
    val iconResourceId = when (verificationState) {
        VerificationState.Initial -> R.drawable.ic_verification_devices
        VerificationState.Canceled -> R.drawable.ic_verification_warning
        VerificationState.AwaitingOtherDeviceResponse -> R.drawable.ic_verification_waiting
        is VerificationState.Verifying, VerificationState.Completed -> R.drawable.ic_verification_emoji
    }
    val titleTextId = when (verificationState) {
        VerificationState.Initial -> StringR.verification_title_initial
        VerificationState.Canceled -> StringR.verification_title_canceled
        VerificationState.AwaitingOtherDeviceResponse -> StringR.verification_title_waiting
        is VerificationState.Verifying, VerificationState.Completed -> StringR.verification_title_verifying
    }
    val subtitleTextId = when (verificationState) {
        VerificationState.Initial -> StringR.verification_subtitle_initial
        VerificationState.Canceled -> StringR.verification_subtitle_canceled
        VerificationState.AwaitingOtherDeviceResponse -> StringR.verification_subtitle_waiting
        is VerificationState.Verifying, VerificationState.Completed -> StringR.verification_subtitle_verifying
    }
    Column(modifier) {
        Spacer(Modifier.height(68.dp))
        Box(
            modifier = Modifier
                .size(width = 70.dp, height = 70.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = LocalColors.current.quinary,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Spacer(modifier = Modifier.height(68.dp))
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 48.dp, height = 48.dp),
                tint = MaterialTheme.colorScheme.secondary,
                resourceId = iconResourceId,
                contentDescription = "",
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = titleTextId),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = ElementTextStyles.Bold.title2,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(id = subtitleTextId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            style = ElementTextStyles.Regular.subheadline,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
internal fun Content(verificationState: VerificationState, modifier: Modifier = Modifier) {
    Spacer(Modifier.height(56.dp))
    when (verificationState) {
        VerificationState.Initial, VerificationState.Canceled, VerificationState.Completed -> Unit
        VerificationState.AwaitingOtherDeviceResponse -> ContentWaiting(modifier)
        is VerificationState.Verifying -> ContentVerifying(verificationState, modifier)
    }
    Spacer(Modifier.height(56.dp))
}

@Composable
internal fun ContentWaiting(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ContentVerifying(verificationState: VerificationState.Verifying, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        mainAxisAlignment = MainAxisAlignment.Center,
        mainAxisSpacing = 32.dp,
        crossAxisSpacing = 40.dp
    ) {
        for (entry in verificationState.emojiList) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(entry.code, fontSize = 34.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(entry.name, style = ElementTextStyles.Regular.body)
            }
        }
    }
}

@Composable
internal fun BottomMenu(screenState: VerifySelfSessionState) {
    val verificationState = screenState.state
    val eventSink = screenState.eventSink

    val buttonsVisible by remember { derivedStateOf { verificationState != VerificationState.AwaitingOtherDeviceResponse } }

    if (buttonsVisible) {
        val isVerifying = (verificationState as? VerificationState.Verifying)?.state is Async.Loading<Boolean>
        val positiveButtonTitle = when (verificationState) {
            VerificationState.Initial -> StringR.verification_positive_button_initial
            VerificationState.Canceled -> StringR.verification_positive_button_canceled
            is VerificationState.Verifying -> {
                if (isVerifying) {
                    StringR.verification_positive_button_verifying_ongoing
                } else {
                    StringR.verification_positive_button_verifying_start
                }
            }
            else -> 0
        }
        val negativeButtonTitle = when (verificationState) {
            VerificationState.Initial -> StringR.verification_negative_button_initial
            VerificationState.Canceled -> StringR.verification_negative_button_canceled
            is VerificationState.Verifying -> StringR.verification_negative_button_verifying
            else -> 0
        }
        val negativeButtonEnabled = !isVerifying

        val positiveButtonEvent = when (verificationState) {
            VerificationState.Initial -> VerifySelfSessionEvents.StartVerification
            is VerificationState.Verifying -> if (!isVerifying) VerifySelfSessionEvents.ConfirmVerification else null
            else -> null
        }

        val negativeButtonEvent = when (verificationState) {
            VerificationState.Initial, VerificationState.Canceled, is VerificationState.Verifying -> VerifySelfSessionEvents.Cancel
            else -> null
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { positiveButtonEvent?.let { eventSink(it) } }
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(stringResource(positiveButtonTitle))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { negativeButtonEvent?.let { eventSink(it) } },
                enabled = negativeButtonEnabled,
            ) {
                Text(stringResource(negativeButtonTitle))
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}

@Preview
@Composable
fun TemplateViewLightPreview(@PreviewParameter(VerifySelfSessionStateProvider::class) state: VerifySelfSessionState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun TemplateViewDarkPreview(@PreviewParameter(VerifySelfSessionStateProvider::class) state: VerifySelfSessionState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: VerifySelfSessionState) {
    VerifySelfSessionScreen(
        state = state,
    )
}
