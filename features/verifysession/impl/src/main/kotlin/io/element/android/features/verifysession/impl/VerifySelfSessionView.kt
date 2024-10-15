/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.emoji.toEmojiResource
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.features.verifysession.impl.VerifySelfSessionState.VerificationStep as FlowStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySelfSessionView(
    state: VerifySelfSessionState,
    onLearnMoreClick: () -> Unit,
    onEnterRecoveryKey: () -> Unit,
    onResetKey: () -> Unit,
    onFinish: () -> Unit,
    onSuccessLogout: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun cancelOrResetFlow() {
        when (state.verificationFlowStep) {
            is FlowStep.Canceled -> state.eventSink(VerifySelfSessionViewEvents.Reset)
            is FlowStep.AwaitingOtherDeviceResponse, FlowStep.Ready -> state.eventSink(VerifySelfSessionViewEvents.Cancel)
            is FlowStep.Verifying -> {
                if (!state.verificationFlowStep.state.isLoading()) {
                    state.eventSink(VerifySelfSessionViewEvents.DeclineVerification)
                }
            }
            else -> Unit
        }
    }

    val latestOnFinish by rememberUpdatedState(newValue = onFinish)
    LaunchedEffect(state.verificationFlowStep, latestOnFinish) {
        if (state.verificationFlowStep is FlowStep.Skipped) {
            latestOnFinish()
        }
    }
    BackHandler {
        cancelOrResetFlow()
    }
    val verificationFlowStep = state.verificationFlowStep

    if (state.verificationFlowStep is FlowStep.Loading ||
        state.verificationFlowStep is FlowStep.Skipped) {
        // Just display a loader in this case, to avoid UI glitch.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        HeaderFooterPage(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        if (state.verificationFlowStep !is FlowStep.Completed &&
                            state.displaySkipButton &&
                            LocalInspectionMode.current.not()) {
                            TextButton(
                                text = stringResource(CommonStrings.action_skip),
                                onClick = { state.eventSink(VerifySelfSessionViewEvents.SkipVerification) }
                            )
                        }
                        if (state.verificationFlowStep is FlowStep.Initial) {
                            TextButton(
                                text = stringResource(CommonStrings.action_signout),
                                onClick = { state.eventSink(VerifySelfSessionViewEvents.SignOut) }
                            )
                        }
                    }
                )
            },
            header = {
                HeaderContent(verificationFlowStep = verificationFlowStep)
            },
            footer = {
                BottomMenu(
                    screenState = state,
                    onCancelClick = ::cancelOrResetFlow,
                    onEnterRecoveryKey = onEnterRecoveryKey,
                    onContinueClick = onFinish,
                    onResetKey = onResetKey,
                )
            }
        ) {
            Content(
                flowState = verificationFlowStep,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }

    when (state.signOutAction) {
        AsyncAction.Loading -> {
            ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        }
        is AsyncAction.Success -> {
            val latestOnSuccessLogout by rememberUpdatedState(onSuccessLogout)
            LaunchedEffect(state) {
                latestOnSuccessLogout(state.signOutAction.data)
            }
        }
        is AsyncAction.Confirming,
        is AsyncAction.Failure,
        AsyncAction.Uninitialized -> Unit
    }
}

@Composable
private fun HeaderContent(verificationFlowStep: FlowStep) {
    val iconStyle = when (verificationFlowStep) {
        VerifySelfSessionState.VerificationStep.Loading -> error("Should not happen")
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> BigIcon.Style.Default(CompoundIcons.LockSolid())
        FlowStep.Canceled -> BigIcon.Style.AlertSolid
        FlowStep.Ready, is FlowStep.Verifying -> BigIcon.Style.Default(CompoundIcons.Reaction())
        FlowStep.Completed -> BigIcon.Style.SuccessSolid
        is FlowStep.Skipped -> return
    }
    val titleTextId = when (verificationFlowStep) {
        VerifySelfSessionState.VerificationStep.Loading -> error("Should not happen")
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_identity_confirmation_title
        FlowStep.Canceled -> CommonStrings.common_verification_cancelled
        FlowStep.Ready -> R.string.screen_session_verification_compare_emojis_title
        FlowStep.Completed -> R.string.screen_identity_confirmed_title
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
        is FlowStep.Skipped -> return
    }
    val subtitleTextId = when (verificationFlowStep) {
        VerifySelfSessionState.VerificationStep.Loading -> error("Should not happen")
        is FlowStep.Initial, FlowStep.AwaitingOtherDeviceResponse -> R.string.screen_identity_confirmation_subtitle
        FlowStep.Canceled -> R.string.screen_session_verification_cancelled_subtitle
        FlowStep.Ready -> R.string.screen_session_verification_ready_subtitle
        FlowStep.Completed -> R.string.screen_identity_confirmed_subtitle
        is FlowStep.Verifying -> when (verificationFlowStep.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_subtitle
        }
        is FlowStep.Skipped -> return
    }

    PageTitle(
        iconStyle = iconStyle,
        title = stringResource(id = titleTextId),
        subtitle = stringResource(id = subtitleTextId)
    )
}

@Composable
private fun Content(
    flowState: FlowStep,
    onLearnMoreClick: () -> Unit,
) {
    when (flowState) {
        is VerifySelfSessionState.VerificationStep.Initial -> {
            ContentInitial(onLearnMoreClick)
        }
        is FlowStep.Verifying -> {
            ContentVerifying(flowState)
        }
        else -> Unit
    }
}

@Composable
private fun ContentInitial(
    onLearnMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier
                .clickable { onLearnMoreClick() }
                .padding(vertical = 4.dp, horizontal = 16.dp),
            text = stringResource(CommonStrings.action_learn_more),
            style = ElementTheme.typography.fontBodyLgMedium
        )
    }
}

@Composable
private fun ContentVerifying(verificationFlowStep: FlowStep.Verifying) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
    onResetKey: () -> Unit,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    val verificationViewState = screenState.verificationFlowStep
    val eventSink = screenState.eventSink

    val isVerifying = (verificationViewState as? FlowStep.Verifying)?.state is AsyncData.Loading<Unit>

    when (verificationViewState) {
        VerifySelfSessionState.VerificationStep.Loading -> error("Should not happen")
        is FlowStep.Initial -> {
            BottomMenu {
                if (verificationViewState.isLastDevice) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_enter_recovery_key),
                        onClick = onEnterRecoveryKey,
                    )
                } else {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_identity_use_another_device),
                        onClick = { eventSink(VerifySelfSessionViewEvents.RequestVerification) },
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_enter_recovery_key),
                        onClick = onEnterRecoveryKey,
                    )
                }
                // This option should always be displayed
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_identity_confirmation_cannot_confirm),
                    onClick = onResetKey,
                )
            }
        }
        is FlowStep.Canceled -> {
            BottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_session_verification_positive_button_canceled),
                    onClick = { eventSink(VerifySelfSessionViewEvents.RequestVerification) },
                )
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_cancel),
                    onClick = onCancelClick,
                )
            }
        }
        is FlowStep.Ready -> {
            BottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_start),
                    onClick = { eventSink(VerifySelfSessionViewEvents.StartSasVerification) },
                )
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_cancel),
                    onClick = onCancelClick,
                )
            }
        }
        is FlowStep.AwaitingOtherDeviceResponse -> {
            BottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_identity_waiting_on_other_device),
                    onClick = {},
                    showProgress = true,
                )
                // Placeholder so the 1st button keeps its vertical position
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        is FlowStep.Verifying -> {
            val positiveButtonTitle = if (isVerifying) {
                stringResource(R.string.screen_session_verification_positive_button_verifying_ongoing)
            } else {
                stringResource(R.string.screen_session_verification_they_match)
            }
            BottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = positiveButtonTitle,
                    showProgress = isVerifying,
                    onClick = {
                        if (!isVerifying) {
                            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
                        }
                    },
                )
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_session_verification_they_dont_match),
                    onClick = { eventSink(VerifySelfSessionViewEvents.DeclineVerification) },
                )
            }
        }
        is FlowStep.Completed -> {
            BottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_continue),
                    onClick = onContinueClick,
                )
                // Placeholder so the 1st button keeps its vertical position
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
        is FlowStep.Skipped -> return
    }
}

@Composable
private fun BottomMenu(
    modifier: Modifier = Modifier,
    buttons: @Composable ColumnScope.() -> Unit,
) {
    ButtonColumnMolecule(
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        buttons()
    }
}

@PreviewsDayNight
@Composable
internal fun VerifySelfSessionViewPreview(@PreviewParameter(VerifySelfSessionStateProvider::class) state: VerifySelfSessionState) = ElementPreview {
    VerifySelfSessionView(
        state = state,
        onLearnMoreClick = {},
        onEnterRecoveryKey = {},
        onResetKey = {},
        onFinish = {},
        onSuccessLogout = {},
    )
}
