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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun IdentityChangeStateView(
    state: IdentityChangeState,
    modifier: Modifier = Modifier,
) {
    // Pick the first identity change to PinViolation
    val identityChange = state.roomMemberIdentityStateChanges.firstOrNull {
        // For now only render PinViolation
        it.identityState == IdentityState.PinViolation
    }
    if (identityChange != null) {
        ComposerAlertMolecule(
            modifier = modifier,
            avatar = identityChange.roomMember.getAvatarData(AvatarSize.ComposerAlert),
            content = buildAnnotatedString {
                val coloredPart = stringResource(CommonStrings.action_learn_more)
                val fullText = stringResource(
                    CommonStrings.crypto_identity_change_pin_violation,
                    identityChange.roomMember.disambiguatedDisplayName,
                    coloredPart,
                )
                val startIndex = fullText.indexOf(coloredPart)
                append(fullText)
                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    ),
                    start = startIndex,
                    end = startIndex + coloredPart.length,
                )
                addStringAnnotation(
                    tag = "LEARN_MORE",
                    annotation = "TODO",
                    start = startIndex,
                    end = startIndex + coloredPart.length
                )
            },
            onSubmitClick = { state.eventSink(IdentityChangeEvent.Submit(identityChange.roomMember.userId)) },
            isCritical = identityChange.identityState == IdentityState.VerificationViolation,
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
    )
}
