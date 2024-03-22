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

package io.element.android.features.verifysession.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.verifysession.impl.emoji.toEmojiResource
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.features.verifysession.impl.VerifySelfSessionState.VerificationStep as FlowStep

@Composable
fun VerifySelfSessionView(
    state: VerifySelfSessionState,
    onEnterRecoveryKey: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun resetFlow() {
        state.eventSink(VerifySelfSessionViewEvents.Reset)
    }
    if (state.verificationFlowStep is FlowStep.Completed) {
        onFinished()
    }
    BackHandler {
        if (state.verificationFlowStep is FlowStep.Canceled) {
            resetFlow()
        }
    }
    val verificationFlowStep = state.verificationFlowStep
    val buttonsVisible by remember(verificationFlowStep) {
        derivedStateOf { verificationFlowStep != FlowStep.AwaitingOtherDeviceResponse && verificationFlowStep != FlowStep.Completed }
    }
    HeaderFooterPage(
        modifier = modifier,
        header = {
            HeaderContent(verificationFlowStep = verificationFlowStep)
        },
        footer = {
            if (buttonsVisible) {
                BottomMenu(
                    screenState = state,
                    goBack = ::resetFlow,
                    onEnterRecoveryKey = onEnterRecoveryKey
                )
            }
        }
    ) {
        Content(flowState = verificationFlowStep)
    }
}

@Composable
private fun HeaderContent(verificationFlowStep: FlowStep) {
    val iconResourceId = when (verificationFlowStep) {
        is FlowStep.Initial -> R.drawable.ic_verification_devices
        FlowStep.Canceled -> R.drawable.ic_verification_warning
        FlowStep.AwaitingOtherDeviceResponse -> R.drawable.ic_verification_waiting
        FlowStep.Ready, is FlowStep.Verifying, FlowStep.Completed -> R.drawable.ic_verification_emoji
    }
    val titleTextId = when (verificationFlowStep) {
        is FlowStep.Initial -> R.string.screen_session_verification_open_existing_session_title
        FlowStep.Canceled -> CommonStrings.common_verification_cancelled
        FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_session_verification_waiting_to_accept_title
        FlowStep.Ready,
        FlowStep.Completed -> R.string.screen_session_verification_compare_emojis_title
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
    }
    val subtitleTextId = when (verificationFlowStep) {
        is FlowStep.Initial -> R.string.screen_session_verification_open_existing_session_subtitle
        FlowStep.Canceled -> R.string.screen_session_verification_cancelled_subtitle
        FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_session_verification_waiting_to_accept_subtitle
        FlowStep.Ready -> R.string.screen_session_verification_ready_subtitle
        FlowStep.Completed -> R.string.screen_session_verification_compare_emojis_subtitle
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_subtitle
        }
    }

    IconTitleSubtitleMolecule(
        modifier = Modifier.padding(top = 60.dp),
        iconResourceId = iconResourceId,
        title = stringResource(id = titleTextId),
        subTitle = stringResource(id = subtitleTextId)
    )
}

@Composable
private fun Content(flowState: FlowStep) {
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
        when (flowState) {
            is FlowStep.Initial, FlowStep.Ready, FlowStep.Canceled, FlowStep.Completed -> Unit
            FlowStep.AwaitingOtherDeviceResponse -> ContentWaiting()
            is FlowStep.Verifying -> ContentVerifying(flowState)
        }
    }
}

@Composable
private fun ContentWaiting() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ContentVerifying(verificationFlowStep: FlowStep.Verifying) {
    when (verificationFlowStep.data) {
        is SessionVerificationData.Decimals -> {
            val text = verificationFlowStep.data.decimals.joinToString(separator = " - ") { it.toString() }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                style = ElementTheme.typography.fontHeadingLgBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
        is SessionVerificationData.Emojis -> {
            // We want each row to have up to 4 emojis
            val rows = verificationFlowStep.data.emojis.chunked(4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(40.dp),
            ) {
                rows.forEach { emojis ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (emoji in emojis) {
                            EmojiItemView(emoji = emoji, modifier = Modifier.widthIn(max = 60.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiItemView(emoji: VerificationEmoji, modifier: Modifier = Modifier) {
    val emojiResource = emoji.number.toEmojiResource()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = emojiResource.drawableRes),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = emojiResource.nameRes),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BottomMenu(
    screenState: VerifySelfSessionState,
    onEnterRecoveryKey: () -> Unit,
    goBack: () -> Unit,
) {
    val verificationViewState = screenState.verificationFlowStep
    val eventSink = screenState.eventSink

    val isVerifying = (verificationViewState as? FlowStep.Verifying)?.state is AsyncData.Loading<Unit>
    val positiveButtonTitle = when (verificationViewState) {
        is FlowStep.Initial -> R.string.screen_session_verification_positive_button_initial
        FlowStep.Canceled -> R.string.screen_session_verification_positive_button_canceled
        is FlowStep.Verifying -> {
            if (isVerifying) {
                R.string.screen_session_verification_positive_button_verifying_ongoing
            } else {
                R.string.screen_session_verification_they_match
            }
        }
        FlowStep.Ready -> CommonStrings.action_start
        else -> null
    }
    val negativeButtonTitle = when (verificationViewState) {
        is FlowStep.Initial -> null
        FlowStep.Canceled -> CommonStrings.action_cancel
        is FlowStep.Verifying -> R.string.screen_session_verification_they_dont_match
        else -> null
    }
    val negativeButtonEnabled = !isVerifying

    val positiveButtonEvent = when (verificationViewState) {
        is FlowStep.Initial -> VerifySelfSessionViewEvents.RequestVerification
        FlowStep.Ready -> VerifySelfSessionViewEvents.StartSasVerification
        is FlowStep.Verifying -> if (!isVerifying) VerifySelfSessionViewEvents.ConfirmVerification else null
        FlowStep.Canceled -> VerifySelfSessionViewEvents.RequestVerification
        else -> null
    }

    val negativeButtonCallback: () -> Unit = when (verificationViewState) {
        is FlowStep.Verifying -> {
            { eventSink(VerifySelfSessionViewEvents.DeclineVerification) }
        }
        else -> goBack
    }

    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        if (positiveButtonTitle != null) {
            Button(
                text = stringResource(positiveButtonTitle),
                showProgress = isVerifying,
                modifier = Modifier.fillMaxWidth(),
                onClick = { positiveButtonEvent?.let { eventSink(it) } }
            )
        }
        if (negativeButtonTitle != null) {
            TextButton(
                text = stringResource(negativeButtonTitle),
                modifier = Modifier.fillMaxWidth(),
                onClick = negativeButtonCallback,
                enabled = negativeButtonEnabled,
            )
        }
        if (verificationViewState is FlowStep.Initial && verificationViewState.canEnterRecoveryKey) {
            Text(
                text = stringResource(id = CommonStrings.common_or),
                color = ElementTheme.colors.textSecondary,
            )
            TextButton(
                text = stringResource(R.string.screen_session_verification_enter_recovery_key),
                modifier = Modifier.fillMaxWidth(),
                onClick = onEnterRecoveryKey,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun VerifySelfSessionViewPreview(@PreviewParameter(VerifySelfSessionStateProvider::class) state: VerifySelfSessionState) = ElementPreview {
    VerifySelfSessionView(
        state = state,
        onEnterRecoveryKey = {},
        onFinished = {},
    )
}
