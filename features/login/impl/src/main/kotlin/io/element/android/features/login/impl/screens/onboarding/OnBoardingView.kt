/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtom
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtomSize
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

// Refs:
// FTUE:
// - https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=133-5427&t=5SHVppfYzjvkEywR-0
// ElementX:
// - https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=1816-97419
@Composable
fun OnBoardingView(
    state: OnBoardingState,
    onBackClick: () -> Unit,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    onReportProblem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loginView = @Composable {
        LoginModeView(
            loginMode = state.loginMode,
            onClearError = {
                state.eventSink(OnBoardingEvents.ClearError)
            },
            onLearnMoreClick = onLearnMoreClick,
            onOidcDetails = onOidcDetails,
            onNeedLoginPassword = onNeedLoginPassword,
            onCreateAccountContinue = onCreateAccountContinue,
        )
    }
    val buttons = @Composable {
        OnBoardingButtons(
            state = state,
            onSignInWithQrCode = onSignInWithQrCode,
            onSignIn = onSignIn,
            onCreateAccount = onCreateAccount,
            onReportProblem = onReportProblem,
        )
    }

    if (state.isAddingAccount) {
        AddOtherAccountScaffold(
            modifier = modifier,
            loginView = loginView,
            buttons = buttons,
            onBackClick = onBackClick,
        )
    } else {
        AddFirstAccountScaffold(
            modifier = modifier,
            state = state,
            loginView = loginView,
            buttons = buttons,
        )
    }
}

@Composable
private fun AddFirstAccountScaffold(
    state: OnBoardingState,
    loginView: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnBoardingPage(
        modifier = modifier,
        renderBackground = state.onBoardingLogoResId == null,
        content = {
            if (state.onBoardingLogoResId != null) {
                OnBoardingLogo(
                    onBoardingLogoResId = state.onBoardingLogoResId,
                )
            } else {
                OnBoardingContent(state = state)
            }
            loginView()
        },
        footer = {
            buttons()
        }
    )
}

@Composable
private fun AddOtherAccountScaffold(
    loginView: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        title = stringResource(CommonStrings.common_add_account),
        iconStyle = BigIcon.Style.Default(CompoundIcons.HomeSolid()),
        buttons = { buttons() },
        content = loginView,
        onBackClick = onBackClick,
    )
}

@Composable
private fun OnBoardingContent(state: OnBoardingState) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = -0.4f
            )
        ) {
            ElementLogoAtom(
                size = ElementLogoAtomSize.Large,
                modifier = Modifier.padding(top = ElementLogoAtomSize.Large.shadowRadius / 2)
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = 0.6f
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.screen_onboarding_welcome_title),
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontHeadingLgBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.screen_onboarding_welcome_message, state.productionApplicationName),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = 17.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnBoardingLogo(
    onBoardingLogoResId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = onBoardingLogoResId),
            contentDescription = null
        )
    }
}

@Composable
private fun OnBoardingButtons(
    state: OnBoardingState,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onReportProblem: () -> Unit,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf {
            state.loginMode is AsyncData.Loading
        }
    }

    ButtonColumnMolecule {
        val signInButtonStringRes = if (state.canLoginWithQrCode || state.canCreateAccount) {
            R.string.screen_onboarding_sign_in_manually
        } else {
            CommonStrings.action_continue
        }
        if (state.canLoginWithQrCode) {
            Button(
                text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code),
                leadingIcon = IconSource.Vector(CompoundIcons.QrCode()),
                onClick = onSignInWithQrCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val defaultAccountProvider = state.defaultAccountProvider
        if (defaultAccountProvider == null) {
            Button(
                text = stringResource(id = signInButtonStringRes),
                onClick = {
                    onSignIn(state.mustChooseAccountProvider)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.onBoardingSignIn)
            )
        } else {
            Button(
                text = stringResource(id = R.string.screen_onboarding_sign_in_to, defaultAccountProvider),
                showProgress = isLoading,
                onClick = {
                    state.eventSink(OnBoardingEvents.OnSignIn(defaultAccountProvider))
                },
                enabled = state.submitEnabled || isLoading,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (state.canCreateAccount) {
            TextButton(
                text = stringResource(id = R.string.screen_onboarding_sign_up),
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (state.isAddingAccount.not()) {
            if (state.canReportBug) {
                // Add a report problem text button. Use a Text since we need a special theme here.
                Text(
                    modifier = Modifier
                        .clickable(onClick = onReportProblem)
                        .padding(16.dp),
                    text = stringResource(id = CommonStrings.common_report_a_problem),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            } else {
                Text(
                    modifier = Modifier
                        .clickable {
                            state.eventSink(OnBoardingEvents.OnVersionClick)
                        }
                        .padding(16.dp),
                    text = stringResource(id = R.string.screen_onboarding_app_version, state.version),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun OnBoardingViewPreview(
    @PreviewParameter(OnBoardingStateProvider::class) state: OnBoardingState
) = ElementPreview {
    OnBoardingView(
        state = state,
        onBackClick = {},
        onSignInWithQrCode = {},
        onSignIn = {},
        onCreateAccount = {},
        onReportProblem = {},
        onOidcDetails = {},
        onNeedLoginPassword = {},
        onLearnMoreClick = {},
        onCreateAccountContinue = {},
    )
}
