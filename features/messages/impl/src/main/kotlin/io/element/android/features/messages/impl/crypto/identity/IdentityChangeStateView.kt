/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun IdentityChangeStateView(
    state: IdentityChangeState,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pick the first identity change to PinViolation
    val pinViolationIdentityChange = state.roomMemberIdentityStateChanges.firstOrNull {
        // For now only render PinViolation
        it.identityState == IdentityState.PinViolation
    }
    if (pinViolationIdentityChange != null) {
        ComposerAlertMolecule(
            modifier = modifier,
            avatar = pinViolationIdentityChange.identityRoomMember.avatarData,
            content = buildAnnotatedString {
                val learnMoreStr = stringResource(CommonStrings.action_learn_more)
                val fullText = stringResource(
                    id = CommonStrings.crypto_identity_change_pin_violation,
                    pinViolationIdentityChange.identityRoomMember.disambiguatedDisplayName,
                    learnMoreStr,
                )
                val learnMoreStartIndex = fullText.indexOf(learnMoreStr)
                append(fullText)
                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    ),
                    start = learnMoreStartIndex,
                    end = learnMoreStartIndex + learnMoreStr.length,
                )
                addLink(
                    url = LinkAnnotation.Url(
                        url = LearnMoreConfig.IDENTITY_CHANGE_URL,
                        linkInteractionListener = {
                            onLinkClick(LearnMoreConfig.IDENTITY_CHANGE_URL)
                        }
                    ),
                    start = learnMoreStartIndex,
                    end = learnMoreStartIndex + learnMoreStr.length,
                )
            },
            onSubmitClick = { state.eventSink(IdentityChangeEvent.Submit(pinViolationIdentityChange.identityRoomMember.userId)) },
            isCritical = pinViolationIdentityChange.identityState == IdentityState.VerificationViolation,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun IdentityChangeStateViewPreview(
    @PreviewParameter(IdentityChangeStateProvider::class) state: IdentityChangeState,
) = ElementPreview {
    IdentityChangeStateView(
        state = state,
        onLinkClick = {},
    )
}
