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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.emoji.toEmojiResource
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
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
    BackHandler {
        when (state.verificationFlowStep) {
            is FlowStep.Canceled -> resetFlow()
            is FlowStep.AwaitingOtherDeviceResponse, FlowStep.Ready -> state.eventSink(VerifySelfSessionViewEvents.Cancel)
            is FlowStep.Verifying -> if (!state.verificationFlowStep.state.isLoading()) { state.eventSink(VerifySelfSessionViewEvents.DeclineVerification) }
            else -> Unit
        }
    }
    val verificationFlowStep = state.verificationFlowStep
    HeaderFooterPage(
        modifier = modifier,
        header = {
            HeaderContent(verificationFlowStep = verificationFlowStep)
        },
        footer = {
            BottomMenu(
                screenState = state,
                goBack = ::resetFlow,
                onEnterRecoveryKey = onEnterRecoveryKey,
                onFinished = onFinished,
            )
        }
    ) {
        Content(flowState = verificationFlowStep)
    }
}

@Composable
private fun HeaderContent(verificationFlowStep: FlowStep) {
    val iconStyle = when (verificationFlowStep) {
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> BigIcon.Style.Default(CompoundIcons.LockSolid())
        FlowStep.Canceled -> BigIcon.Style.AlertSolid
        FlowStep.Ready, is FlowStep.Verifying -> BigIcon.Style.Default(CompoundIcons.Reaction())
        FlowStep.Completed -> BigIcon.Style.SuccessSolid
    }
    val titleTextId = when (verificationFlowStep) {
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_identity_confirmation_title
        FlowStep.Canceled -> CommonStrings.common_verification_cancelled
        FlowStep.Ready -> R.string.screen_session_verification_compare_emojis_title
        FlowStep.Completed -> R.string.screen_identity_confirmed_title
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
    }
    val subtitleTextId = when (verificationFlowStep) {
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_identity_confirmation_subtitle
        FlowStep.Canceled -> R.string.screen_session_verification_cancelled_subtitle
        FlowStep.Ready -> R.string.screen_session_verification_ready_subtitle
        FlowStep.Completed -> R.string.screen_identity_confirmation_subtitle
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_subtitle
        }
    }

    PageTitle(
        modifier = Modifier.padding(top = 60.dp),
        iconStyle = iconStyle,
        title = stringResource(id = titleTextId),
        subtitle = stringResource(id = subtitleTextId)
    )
}

@Composable
private fun Content(flowState: FlowStep) {
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
        when (flowState) {
            is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse, FlowStep.Ready, FlowStep.Canceled, FlowStep.Completed -> Unit
            is FlowStep.Verifying -> ContentVerifying(flowState)
        }
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
    onFinished: () -> Unit,
) {
    val verificationViewState = screenState.verificationFlowStep
    val eventSink = screenState.eventSink

    val isVerifying = (verificationViewState as? FlowStep.Verifying)?.state is AsyncData.Loading<Unit>

    when (verificationViewState) {
        is FlowStep.Initial -> {
            BottomMenu(
                positiveButtonTitle = stringResource(R.string.screen_identity_use_another_device),
                onPositiveButtonClicked = { eventSink(VerifySelfSessionViewEvents.RequestVerification) },
                negativeButtonTitle = stringResource(R.string.screen_session_verification_enter_recovery_key),
                onNegativeButtonClicked = onEnterRecoveryKey,
            )
        }
        is FlowStep.Canceled -> {
            BottomMenu(
                positiveButtonTitle = stringResource(R.string.screen_session_verification_positive_button_canceled),
                onPositiveButtonClicked = { eventSink(VerifySelfSessionViewEvents.RequestVerification) },
                negativeButtonTitle = stringResource(CommonStrings.action_cancel),
                onNegativeButtonClicked = goBack,
            )
        }
        is FlowStep.Ready -> {
            BottomMenu(
                positiveButtonTitle = stringResource(CommonStrings.action_start),
                onPositiveButtonClicked = { eventSink(VerifySelfSessionViewEvents.StartSasVerification) },
                negativeButtonTitle = stringResource(CommonStrings.action_cancel),
                onNegativeButtonClicked = goBack,
            )
        }
        is FlowStep.AwaitingOtherDeviceResponse -> {
            BottomMenu(
                positiveButtonTitle = stringResource(R.string.screen_identity_waiting_on_other_device),
                onPositiveButtonClicked = {},
                isLoading = true,
            )
        }
        is FlowStep.Verifying -> {
            BottomMenu(
                positiveButtonTitle = stringResource(R.string.screen_session_verification_they_match),
                onPositiveButtonClicked = { if (!isVerifying) { eventSink(VerifySelfSessionViewEvents.ConfirmVerification) } },
                negativeButtonTitle = stringResource(R.string.screen_session_verification_they_dont_match),
                onNegativeButtonClicked = { eventSink(VerifySelfSessionViewEvents.DeclineVerification) },
                isLoading = isVerifying,
            )
        }
        is FlowStep.Completed -> {
            BottomMenu(
                positiveButtonTitle = stringResource(CommonStrings.action_continue),
                onPositiveButtonClicked = onFinished,
            )
        }
        else -> Unit
    }
}

@Composable
private fun BottomMenu(
    positiveButtonTitle: String?,
    onPositiveButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
    negativeButtonTitle: String? = null,
    negativeButtonEnabled: Boolean = negativeButtonTitle != null,
    onNegativeButtonClicked: () -> Unit = {},
    isLoading: Boolean = false,
) {
    ButtonColumnMolecule(
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        if (positiveButtonTitle != null) {
            Button(
                text = positiveButtonTitle,
                showProgress = isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = onPositiveButtonClicked,
            )
        }
        if (negativeButtonTitle != null) {
            TextButton(
                text = negativeButtonTitle,
                modifier = Modifier.fillMaxWidth(),
                onClick = onNegativeButtonClicked,
                enabled = negativeButtonEnabled,
            )
        } else {
            Spacer(modifier = Modifier.height(48.dp))
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
