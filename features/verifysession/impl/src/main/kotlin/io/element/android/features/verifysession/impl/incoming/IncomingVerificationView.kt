/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationState.Step
import io.element.android.features.verifysession.impl.incoming.ui.SessionDetailsView
import io.element.android.features.verifysession.impl.ui.VerificationBottomMenu
import io.element.android.features.verifysession.impl.ui.VerificationContentVerifying
import io.element.android.features.verifysession.impl.ui.VerificationUserProfileContent
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.InvisibleButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationRequest
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
                navigationIcon = {
                    when {
                        step is Step.Initial && !step.isWaiting -> Unit
                        step is Step.Completed -> Unit
                        else -> BackButton(onClick = { state.eventSink(IncomingVerificationViewEvents.GoBack) })
                    }
                },
                colors = topAppBarColors(containerColor = Color.Transparent),
            )
        },
        header = {
            IncomingVerificationHeader(step = step, request = state.request)
        },
        footer = {
            IncomingVerificationBottomMenu(
                state = state,
            )
        },
        isScrollable = true,
    ) {
        IncomingVerificationContent(
            step = step,
            request = state.request,
        )
    }
}

@Composable
private fun IncomingVerificationHeader(step: Step, request: VerificationRequest.Incoming) {
    val iconStyle = when (step) {
        Step.Canceled -> BigIcon.Style.AlertSolid
        is Step.Initial -> if (step.isWaiting) {
            BigIcon.Style.Loading
        } else {
            when (request) {
                is VerificationRequest.Incoming.OtherSession -> BigIcon.Style.Default(CompoundIcons.LockSolid())
                is VerificationRequest.Incoming.User -> BigIcon.Style.Default(CompoundIcons.UserProfileSolid())
            }
        }
        is Step.Verifying -> if (step.isWaiting) {
            BigIcon.Style.Loading
        } else {
            BigIcon.Style.Default(CompoundIcons.ReactionSolid())
        }
        Step.Completed -> BigIcon.Style.SuccessSolid
        Step.Failure -> BigIcon.Style.AlertSolid
    }
    val titleTextId = when (step) {
        Step.Canceled -> CommonStrings.common_verification_failed
        is Step.Initial -> R.string.screen_session_verification_request_title
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_title
            is SessionVerificationData.Emojis -> R.string.screen_session_verification_compare_emojis_title
        }
        Step.Completed -> CommonStrings.common_verification_complete
        Step.Failure -> R.string.screen_session_verification_request_failure_title
    }
    val subtitleTextId = when (step) {
        Step.Canceled -> R.string.screen_session_verification_request_failure_subtitle
        is Step.Initial -> when (request) {
            is VerificationRequest.Incoming.OtherSession -> R.string.screen_session_verification_request_subtitle
            is VerificationRequest.Incoming.User -> R.string.screen_session_verification_user_responder_subtitle
        }
        is Step.Verifying -> when (step.data) {
            is SessionVerificationData.Decimals -> R.string.screen_session_verification_compare_numbers_subtitle
            is SessionVerificationData.Emojis -> when (request) {
                is VerificationRequest.Incoming.OtherSession -> R.string.screen_session_verification_compare_emojis_subtitle
                is VerificationRequest.Incoming.User -> R.string.screen_session_verification_compare_emojis_user_subtitle
            }
        }
        Step.Completed -> when (request) {
            is VerificationRequest.Incoming.OtherSession -> R.string.screen_session_verification_complete_subtitle
            is VerificationRequest.Incoming.User -> R.string.screen_session_verification_complete_user_subtitle
        }
        Step.Failure -> R.string.screen_session_verification_request_failure_subtitle
    }
    val timeLimitMessage = if (step.isTimeLimited) {
        stringResource(CommonStrings.a11y_session_verification_time_limited_action_required)
    } else {
        ""
    }
    IconTitleSubtitleMolecule(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = timeLimitMessage
                focused = true
                if (iconStyle == BigIcon.Style.Loading) {
                    // Same code than Modifier.progressSemantics()
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }
            }
            .focusable(),
        iconStyle = iconStyle,
        title = stringResource(id = titleTextId),
        subTitle = stringResource(id = subtitleTextId),
    )
}

@Composable
private fun IncomingVerificationContent(
    step: Step,
    request: VerificationRequest.Incoming,
) {
    when (step) {
        is Step.Initial -> ContentInitial(step, request)
        is Step.Verifying -> VerificationContentVerifying(step.data)
        else -> Unit
    }
}

@Composable
private fun ContentInitial(
    initialIncoming: Step.Initial,
    request: VerificationRequest.Incoming,
) {
    when (request) {
        is VerificationRequest.Incoming.OtherSession -> {
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
        is VerificationRequest.Incoming.User -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                VerificationUserProfileContent(
                    user = request.details.senderProfile,
                )
            }
        }
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
                // Show nothing
            } else {
                VerificationBottomMenu {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(CommonStrings.action_start_verification),
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
                // Add invisible buttons to keep the same screen layout
                VerificationBottomMenu {
                    InvisibleButton()
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

@Preview
@Composable
internal fun IncomingVerificationViewA11yPreview() = ElementPreview {
    IncomingVerificationView(
        state = anIncomingVerificationState(),
    )
}
