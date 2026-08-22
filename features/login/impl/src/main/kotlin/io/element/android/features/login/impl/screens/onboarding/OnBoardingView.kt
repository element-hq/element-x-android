/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 *
 * Modified by Feral: first-account onboarding renders the Feral dark gradient, logo,
 * "FERAL / FOR FERALISTS" header and frosted-glass buttons (see Feral*.kt in this package);
 * the single-provider sign-in button uses the plain "Sign in" label.
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialogView
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=41-6503
 */
@Composable
fun OnBoardingView(
    state: OnBoardingState,
    onBackClick: () -> Unit,
    onDeveloperSettingsClick: () -> Unit,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onOAuthDetails: (OAuthDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onReportProblem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loginView = @Composable {
        LoginModeView(
            loginMode = state.loginModeState.loginMode,
            onClearError = {
                state.eventSink(OnBoardingEvents.ClearError)
            },
            onLearnMoreClick = onLearnMoreClick,
            onOAuthDetails = onOAuthDetails,
            onNeedLoginPassword = onNeedLoginPassword,
        )
        LocalNetworkPermissionDialogView(
            dialog = state.loginModeState.localNetworkPermissionDialog,
            onSubmit = {
                state.loginModeState.eventSink(LoginModeEvent.RequestLocalNetworkPermission)
            },
            onDismiss = {
                state.loginModeState.eventSink(LoginModeEvent.DismissLocalNetworkPermission)
            }
        )
    }
    val buttons = @Composable {
        OnBoardingButtons(
            state = state,
            onDarkBackground = !state.isAddingAccount,
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
            onBackClick = onBackClick,
            onDeveloperSettingsClick = onDeveloperSettingsClick,
        )
    }
}

@Composable
private fun AddFirstAccountScaffold(
    state: OnBoardingState,
    loginView: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    onBackClick: () -> Unit,
    onDeveloperSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeralOnBoardingBackground(modifier = modifier) {
        FeralOnBoardingPage(
            content = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.onBoardingLogoResId != null) {
                        OnBoardingLogo(
                            onBoardingLogoResId = state.onBoardingLogoResId,
                        )
                    } else {
                        FeralOnBoardingContent()
                    }
                    if (state.showDeveloperSettings) {
                        IconButton(
                            onClick = onDeveloperSettingsClick,
                            modifier = Modifier
                                .align(Alignment.TopStart),
                        ) {
                            Icon(
                                imageVector = CompoundIcons.SettingsSolid(),
                                contentDescription = stringResource(CommonStrings.common_developer_options),
                                tint = Color.White,
                            )
                        }
                    }
                    if (state.showBackButton) {
                        // Add icon button to "navigate back"
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd),
                        ) {
                            Icon(
                                imageVector = CompoundIcons.Close(),
                                contentDescription = stringResource(CommonStrings.action_cancel),
                                tint = Color.White,
                            )
                        }
                    }
                }
                loginView()
            },
            footer = {
                buttons()
            }
        )
    }
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
private fun FeralOnBoardingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(2f))
        FeralLogo(
            modifier = Modifier.padding(bottom = 32.dp),
        )
        Text(
            text = stringResource(R.string.feral_onboarding_welcome_title),
            style = FeralTypography.welcomeTitle.copy(
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.feral_onboarding_welcome_subtitle).uppercase(),
            style = FeralTypography.sectionTitle.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp,
            ),
            color = Color.White.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(2f))
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
    onDarkBackground: Boolean,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onReportProblem: () -> Unit,
) {
    val isLoading by remember(state.loginModeState.loginMode) {
        derivedStateOf {
            state.loginModeState.loginMode is AsyncData.Loading
        }
    }

    val secondaryTextStyle = if (onDarkBackground) {
        FeralTypography.sectionTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    } else {
        ElementTheme.typography.fontBodySmRegular
    }
    val secondaryTextColor = if (onDarkBackground) Color.White.copy(alpha = 0.6f) else ElementTheme.colors.textSecondary

    ButtonColumnMolecule {
        val signInButtonStringRes = if (state.canLoginWithQrCode || state.canCreateAccount) {
            R.string.screen_onboarding_sign_in_manually
        } else {
            CommonStrings.action_continue
        }
        if (state.canLoginWithQrCode) {
            FeralOnBoardingButton(
                text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code),
                onDarkBackground = onDarkBackground,
                leadingIcon = IconSource.Vector(CompoundIcons.QrCode()),
                onClick = onSignInWithQrCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val defaultAccountProvider = state.defaultAccountProvider
        if (defaultAccountProvider == null) {
            FeralOnBoardingButton(
                text = stringResource(id = signInButtonStringRes),
                onDarkBackground = onDarkBackground,
                onClick = {
                    onSignIn(state.mustChooseAccountProvider)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.onBoardingSignIn)
            )
        } else {
            // Feral: plain "Sign in" label, the homeserver is not shown on the landing page.
            FeralOnBoardingButton(
                text = stringResource(id = CommonStrings.screen_change_server_navigation_title_login),
                onDarkBackground = onDarkBackground,
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
            FeralOnBoardingTextButton(
                text = stringResource(id = R.string.screen_onboarding_sign_up),
                onClick = onCreateAccount,
                onDarkBackground = onDarkBackground,
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
                    style = secondaryTextStyle,
                    color = secondaryTextColor,
                )
            } else {
                Text(
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            state.eventSink(OnBoardingEvents.OnVersionClick)
                        }
                        .padding(16.dp),
                    text = stringResource(id = R.string.screen_onboarding_app_version, state.version),
                    style = secondaryTextStyle,
                    color = secondaryTextColor,
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
        onDeveloperSettingsClick = {},
        onSignInWithQrCode = {},
        onSignIn = {},
        onCreateAccount = {},
        onReportProblem = {},
        onOAuthDetails = {},
        onNeedLoginPassword = {},
        onLearnMoreClick = {},
    )
}
