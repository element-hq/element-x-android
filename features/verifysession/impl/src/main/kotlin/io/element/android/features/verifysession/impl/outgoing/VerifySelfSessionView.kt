/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionState.Step
import io.element.android.features.verifysession.impl.ui.VerificationBottomMenu
import io.element.android.features.verifysession.impl.ui.VerificationContentVerifying
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.InvisibleButton
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.ui.strings.CommonStrings

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
    val step = state.step
    fun cancelOrResetFlow() {
        when (step) {
            is Step.Canceled -> state.eventSink(VerifySelfSessionViewEvents.Reset)
            is Step.AwaitingOtherDeviceResponse,
            Step.UseAnotherDevice,
            Step.Ready -> state.eventSink(VerifySelfSessionViewEvents.Cancel)
            is Step.Verifying -> {
                if (!step.state.isLoading()) {
                    state.eventSink(VerifySelfSessionViewEvents.DeclineVerification)
                }
            }
            else -> Unit
        }
    }

    val latestOnFinish by rememberUpdatedState(newValue = onFinish)
    LaunchedEffect(step, latestOnFinish) {
        if (step is Step.Skipped) {
            latestOnFinish()
        }
    }
    BackHandler {
        cancelOrResetFlow()
    }

    if (step is Step.Loading ||
        step is Step.Skipped) {
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
                        if (step !is Step.Completed &&
                            state.displaySkipButton &&
                            LocalInspectionMode.current.not()) {
                            TextButton(
                                text = stringResource(CommonStrings.action_skip),
                                onClick = { state.eventSink(VerifySelfSessionViewEvents.SkipVerification) }
                            )
                        }
                        if (step is Step.Initial) {
                            TextButton(
                                text = stringResource(CommonStrings.action_signout),
                                onClick = { state.eventSink(VerifySelfSessionViewEvents.SignOut) }
                            )
                        }
                    }
                )
            },
            header = {
                VerifySelfSessionHeader(step = step)
            },
            footer = {
                VerifySelfSessionBottomMenu(
                    screenState = state,
                    onCancelClick = ::cancelOrResetFlow,
                    onEnterRecoveryKey = onEnterRecoveryKey,
                    onContinueClick = onFinish,
                    onResetKey = onResetKey,
                )
            }
        ) {
            VerifySelfSessionContent(
                flowState = step,
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
private fun VerifySelfSessionHeader(step: Step) {
    val iconStyle = when (step) {
        Step.Loading -> error("Should not happen")
        is Step.Initial -> BigIcon.Style.Default(CompoundIcons.LockSolid())
        Step.UseAnotherDevice -> BigIcon.Style.Default(CompoundIcons.Devices())
        Step.AwaitingOtherDeviceResponse -> BigIcon.Style.Default(CompoundIcons.Devices())
        Step.Canceled -> BigIcon.Style.AlertSolid
        Step.Ready, is Step.Verifying -> BigIcon.Style.Default(CompoundIcons.Reaction())
        Step.Completed -> BigIcon.Style.SuccessSolid
        is Step.Skipped -> return
    }
    val titleTextId = when (step) {
        Step.Loading -> error("Should not happen")
        is Step.Initial -> R.string.screen_identity_confirmation_title
        Step.UseAnotherDevice -> R.string.screen_session_verification_use_another_device_title
        Step.AwaitingOtherDeviceResponse -> R.string.screen_session_verification_waiting_another_device_title
        Step.Canceled -> CommonStrings.common_verification_failed
        Step.Ready -> R.string.screen_session_verification_compare_emojis_title
        Step.Completed -> R.string.screen_identity_confirmed_title
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
        is Step.Skipped -> return
    }
    val subtitleTextId = when (step) {
        Step.Loading -> error("Should not happen")
        is Step.Initial -> R.string.screen_identity_confirmation_subtitle
        Step.UseAnotherDevice -> R.string.screen_session_verification_use_another_device_subtitle
        Step.AwaitingOtherDeviceResponse -> R.string.screen_session_verification_waiting_another_device_subtitle
        Step.Canceled -> R.string.screen_session_verification_failed_subtitle
        Step.Ready -> R.string.screen_session_verification_ready_subtitle
        Step.Completed -> R.string.screen_identity_confirmed_subtitle
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_subtitle
        }
        is Step.Skipped -> return
    }

    PageTitle(
        iconStyle = iconStyle,
        title = stringResource(id = titleTextId),
        subtitle = stringResource(id = subtitleTextId)
    )
}

@Composable
private fun VerifySelfSessionContent(
    flowState: Step,
    onLearnMoreClick: () -> Unit,
) {
    when (flowState) {
        is Step.Initial -> {
            ContentInitial(onLearnMoreClick)
        }
        is Step.Verifying -> {
            VerificationContentVerifying(flowState.data)
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
private fun VerifySelfSessionBottomMenu(
    screenState: VerifySelfSessionState,
    onEnterRecoveryKey: () -> Unit,
    onResetKey: () -> Unit,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    val verificationViewState = screenState.step
    val eventSink = screenState.eventSink

    val isVerifying = (verificationViewState as? Step.Verifying)?.state is AsyncData.Loading<Unit>

    when (verificationViewState) {
        Step.Loading -> error("Should not happen")
        is Step.Initial -> {
            VerificationBottomMenu {
                if (verificationViewState.isLastDevice.not()) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_identity_use_another_device),
                        onClick = { eventSink(VerifySelfSessionViewEvents.UseAnotherDevice) },
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_session_verification_enter_recovery_key),
                    onClick = onEnterRecoveryKey,
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_identity_confirmation_cannot_confirm),
                    onClick = onResetKey,
                )
            }
        }
        is Step.UseAnotherDevice -> {
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_start_verification),
                    onClick = { eventSink(VerifySelfSessionViewEvents.RequestVerification) },
                )
                InvisibleButton()
            }
        }
        is Step.Canceled -> {
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_done),
                    onClick = onCancelClick,
                )
                InvisibleButton()
            }
        }
        is Step.Ready -> {
            VerificationBottomMenu {
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
        is Step.AwaitingOtherDeviceResponse -> {
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_identity_waiting_on_other_device),
                    onClick = {},
                    showProgress = true,
                    enabled = false,
                )
                InvisibleButton()
            }
        }
        is Step.Verifying -> {
            val positiveButtonTitle = if (isVerifying) {
                stringResource(R.string.screen_session_verification_positive_button_verifying_ongoing)
            } else {
                stringResource(R.string.screen_session_verification_they_match)
            }
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = positiveButtonTitle,
                    showProgress = isVerifying,
                    enabled = !isVerifying,
                    onClick = {
                        if (!isVerifying) {
                            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
                        }
                    },
                )
                if (isVerifying) {
                    InvisibleButton()
                } else {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_they_dont_match),
                        onClick = { eventSink(VerifySelfSessionViewEvents.DeclineVerification) },
                    )
                }
            }
        }
        is Step.Completed -> {
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_continue),
                    onClick = onContinueClick,
                )
                InvisibleButton()
            }
        }
        is Step.Skipped -> return
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
