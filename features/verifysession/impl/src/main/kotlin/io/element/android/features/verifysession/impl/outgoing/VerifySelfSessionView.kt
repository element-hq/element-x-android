/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionState.Step
import io.element.android.features.verifysession.impl.ui.VerificationBottomMenu
import io.element.android.features.verifysession.impl.ui.VerificationContentVerifying
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.InvisibleButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySelfSessionView(
    state: VerifySelfSessionState,
    onLearnMoreClick: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = state.step
    fun cancelOrResetFlow() {
        when (step) {
            is Step.Canceled -> state.eventSink(VerifySelfSessionViewEvents.Reset)
            Step.Initial, Step.Completed -> onBack()
            Step.Ready, is Step.AwaitingOtherDeviceResponse -> state.eventSink(VerifySelfSessionViewEvents.Cancel)
            is Step.Verifying -> {
                if (!step.state.isLoading()) {
                    state.eventSink(VerifySelfSessionViewEvents.DeclineVerification)
                }
            }
            else -> Unit
        }
    }

    BackHandler {
        cancelOrResetFlow()
    }

    if (step is Step.Loading) {
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
                    navigationIcon = if (step != Step.Completed) {
                        { BackButton(onClick = ::cancelOrResetFlow) }
                    } else {
                        {}
                    }
                )
            },
            header = {
                VerifySelfSessionHeader(step = step, request = state.request)
            },
            footer = {
                VerifySelfSessionBottomMenu(
                    screenState = state,
                    onCancelClick = ::cancelOrResetFlow,
                    onContinueClick = onFinish,
                )
            }
        ) {
            VerifySelfSessionContent(
                flowState = step,
                request = state.request,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}

@Composable
private fun VerifySelfSessionHeader(step: Step, request: VerificationRequest.Outgoing) {
    val iconStyle = when (step) {
        Step.Loading -> error("Should not happen")
        Step.Initial -> when (request) {
            is VerificationRequest.Outgoing.CurrentSession -> BigIcon.Style.Default(CompoundIcons.Devices())
            is VerificationRequest.Outgoing.User -> BigIcon.Style.Default(CompoundIcons.LockSolid())
        }
        Step.AwaitingOtherDeviceResponse -> BigIcon.Style.Loading
        Step.Canceled -> BigIcon.Style.AlertSolid
        Step.Ready -> BigIcon.Style.Default(CompoundIcons.Reaction())
        Step.Completed -> BigIcon.Style.SuccessSolid
        is Step.Verifying -> {
            if (step.state is AsyncData.Loading<Unit>) {
                BigIcon.Style.Loading
            } else {
                BigIcon.Style.Default(CompoundIcons.Reaction())
            }
        }
        is Step.Exit -> return
    }
    val titleTextId = when (step) {
        Step.Loading -> error("Should not happen")
        Step.Initial -> when (request) {
            is VerificationRequest.Outgoing.CurrentSession -> R.string.screen_session_verification_use_another_device_title
            is VerificationRequest.Outgoing.User -> R.string.screen_session_verification_user_initiator_title
        }
        Step.AwaitingOtherDeviceResponse -> when (request) {
            is VerificationRequest.Outgoing.CurrentSession -> R.string.screen_session_verification_waiting_another_device_title
            is VerificationRequest.Outgoing.User -> R.string.screen_session_verification_waiting_other_user_title
        }
        Step.Canceled -> CommonStrings.common_verification_failed
        Step.Ready -> R.string.screen_session_verification_compare_emojis_title
        Step.Completed -> CommonStrings.common_verification_complete
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
        is Step.Exit -> return
    }
    val subtitleTextId = when (step) {
        Step.Loading -> error("Should not happen")
        Step.Initial -> when (request) {
            is VerificationRequest.Outgoing.CurrentSession -> R.string.screen_session_verification_use_another_device_subtitle
            is VerificationRequest.Outgoing.User -> R.string.screen_session_verification_user_initiator_subtitle
        }
        Step.AwaitingOtherDeviceResponse -> R.string.screen_session_verification_waiting_subtitle
        Step.Canceled -> R.string.screen_session_verification_failed_subtitle
        Step.Ready -> R.string.screen_session_verification_ready_subtitle
        Step.Completed -> when (request) {
            is VerificationRequest.Outgoing.CurrentSession -> R.string.screen_identity_confirmed_subtitle
            is VerificationRequest.Outgoing.User -> R.string.screen_session_verification_complete_user_subtitle
        }
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> when (request) {
                is VerificationRequest.Outgoing.CurrentSession -> R.string.screen_session_verification_compare_emojis_subtitle
                is VerificationRequest.Outgoing.User -> R.string.screen_session_verification_compare_emojis_user_subtitle
            }
        }
        is Step.Exit -> return
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
    request: VerificationRequest.Outgoing,
    onLearnMoreClick: () -> Unit,
) {
    when (flowState) {
        is Step.Initial -> {
            when (request) {
                is VerificationRequest.Outgoing.CurrentSession -> Unit
                is VerificationRequest.Outgoing.User -> ContentInitial(onLearnMoreClick)
            }
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
        is Step.AwaitingOtherDeviceResponse -> Unit
        is Step.Verifying -> {
            if (isVerifying) {
                // Show nothing
            } else {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_they_match),
                        onClick = {
                            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
                        },
                    )

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
        is Step.Exit -> return
    }
}

@PreviewsDayNight
@Composable
internal fun VerifySelfSessionViewPreview(@PreviewParameter(VerifySelfSessionStateProvider::class) state: VerifySelfSessionState) = ElementPreview {
    VerifySelfSessionView(
        state = state,
        onLearnMoreClick = {},
        onFinish = {},
        onBack = {},
    )
}
