/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ConfirmAccountProviderView(
    state: ConfirmAccountProviderState,
    onOAuthDetails: (OAuthDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf {
            state.loginMode is AsyncData.Loading
        }
    }
    val eventSink = state.eventSink

    HeaderFooterPage(
        modifier = modifier,
        header = {
            // Alpha-branded landing — text wordmark + tagline. The previous Element
            // version embedded the homeserver URL ("You're about to sign in to
            // 192.168.1.65:8008") which broke immersion for a consumer audience.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.screen_alpha_wordmark),
                    color = ElementTheme.colors.iconAccentPrimary,
                    style = ElementTheme.typography.fontHeadingLgBold.copy(fontSize = 56.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.screen_alpha_signin_title),
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontHeadingMdBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.screen_alpha_signin_subtitle),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    textAlign = TextAlign.Center,
                )
            }
        },
        footer = {
            ButtonColumnMolecule {
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    showProgress = isLoading,
                    onClick = { eventSink.invoke(ConfirmAccountProviderEvents.Continue) },
                    enabled = state.submitEnabled || isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginContinue)
                )
                // Drop the "Change account provider" entry: Alpha ships against a
                // single bundled homeserver, exposing the URL switcher just invites
                // first-time users to type a random server and wonder why it fails.
            }
        }
    ) {
        LoginModeView(
            loginMode = state.loginMode,
            onClearError = {
                eventSink(ConfirmAccountProviderEvents.ClearError)
            },
            onLearnMoreClick = onLearnMoreClick,
            onOAuthDetails = onOAuthDetails,
            onNeedLoginPassword = onNeedLoginPassword,
            onCreateAccountContinue = onCreateAccountContinue,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ConfirmAccountProviderViewPreview(
    @PreviewParameter(ConfirmAccountProviderStateProvider::class) state: ConfirmAccountProviderState
) = ElementPreview {
    ConfirmAccountProviderView(
        state = state,
        onOAuthDetails = {},
        onNeedLoginPassword = {},
        onCreateAccountContinue = {},
        onLearnMoreClick = {},
        onChange = {},
    )
}
