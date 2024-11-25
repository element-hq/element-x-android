/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationState.Step
import io.element.android.features.verifysession.impl.incoming.ui.SessionDetailsView
import io.element.android.features.verifysession.impl.ui.VerificationBottomMenu
import io.element.android.features.verifysession.impl.ui.VerificationContentVerifying
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.InvisibleButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * [Figma](https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=819-7324).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingVerificationView(
    state: IncomingVerificationState,
    modifier: Modifier = Modifier,
) {
    val step = state.step

    BackHandler {
        state.eventSink(IncomingVerificationViewEvents.GoBack)
    }
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
            )
        },
        header = {
            IncomingVerificationHeader(step = step)
        },
        footer = {
            IncomingVerificationBottomMenu(
                state = state,
            )
        }
    ) {
        IncomingVerificationContent(
            step = step,
        )
    }
}

@Composable
private fun IncomingVerificationHeader(step: Step) {
    val iconStyle = when (step) {
        Step.Canceled -> BigIcon.Style.AlertSolid
        is Step.Initial -> BigIcon.Style.Default(CompoundIcons.LockSolid())
        is Step.Verifying -> BigIcon.Style.Default(CompoundIcons.Reaction())
        Step.Completed -> BigIcon.Style.SuccessSolid
        Step.Failure -> BigIcon.Style.AlertSolid
    }
    val titleTextId = when (step) {
        Step.Canceled -> R.string.screen_session_verification_request_failure_title
        is Step.Initial -> R.string.screen_session_verification_request_title
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
        Step.Completed -> R.string.screen_session_verification_request_success_title
        Step.Failure -> R.string.screen_session_verification_request_failure_title
    }
    val subtitleTextId = when (step) {
        Step.Canceled -> R.string.screen_session_verification_request_failure_subtitle
        is Step.Initial -> R.string.screen_session_verification_request_subtitle
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_subtitle
        }
        Step.Completed -> R.string.screen_session_verification_request_success_subtitle
        Step.Failure -> R.string.screen_session_verification_request_failure_subtitle
    }
    PageTitle(
        iconStyle = iconStyle,
        title = stringResource(id = titleTextId),
        subtitle = stringResource(id = subtitleTextId)
    )
}

@Composable
private fun IncomingVerificationContent(
    step: Step,
) {
    when (step) {
        is Step.Initial -> ContentInitial(step)
        is Step.Verifying -> VerificationContentVerifying(step.data)
        else -> Unit
    }
}

@Composable
private fun ContentInitial(
    initialIncoming: Step.Initial,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SessionDetailsView(
            deviceName = initialIncoming.deviceDisplayName,
            deviceId = initialIncoming.deviceId,
            signInFormattedTimestamp = initialIncoming.formattedSignInTime,
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            text = stringResource(R.string.screen_session_verification_request_footer),
            style = ElementTheme.typography.fontBodyMdMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IncomingVerificationBottomMenu(
    state: IncomingVerificationState,
) {
    val step = state.step
    val eventSink = state.eventSink

    when (step) {
        is Step.Initial -> {
            if (step.isWaiting) {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_identity_waiting_on_other_device),
                        onClick = {},
                        enabled = false,
                        showProgress = true,
                    )
                    InvisibleButton()
                }
            } else {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(CommonStrings.action_start),
                        onClick = { eventSink(IncomingVerificationViewEvents.StartVerification) },
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(CommonStrings.action_ignore),
                        onClick = { eventSink(IncomingVerificationViewEvents.IgnoreVerification) },
                    )
                }
            }
        }
        is Step.Verifying -> {
            if (step.isWaiting) {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_positive_button_verifying_ongoing),
                        onClick = {},
                        enabled = false,
                        showProgress = true,
                    )
                    InvisibleButton()
                }
            } else {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_they_match),
                        onClick = { eventSink(IncomingVerificationViewEvents.ConfirmVerification) },
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.screen_session_verification_they_dont_match),
                        onClick = { eventSink(IncomingVerificationViewEvents.DeclineVerification) },
                    )
                }
            }
        }
        Step.Canceled,
        is Step.Completed,
        is Step.Failure -> {
            VerificationBottomMenu {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_done),
                    onClick = { eventSink(IncomingVerificationViewEvents.GoBack) },
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun IncomingVerificationViewPreview(@PreviewParameter(IncomingVerificationStateProvider::class) state: IncomingVerificationState) = ElementPreview {
    IncomingVerificationView(
        state = state,
    )
}
