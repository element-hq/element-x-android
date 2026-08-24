/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.changeserver.ChangeServerView
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialogView
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Account provider entry screen, shared by sign in and account creation.
 * Figma (Compound Android Templates):
 * - Sign in: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=41-10251
 * - Create account: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=41-10239
 */
@Composable
fun ConfirmAccountProviderView(
    state: ConfirmAccountProviderState,
    onOAuthDetails: (OAuthDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var input by textFieldState(stateValue = state.accountProviderInput)

    // Inline autocomplete: the un-typed remainder of the suggested account provider (rendered in grey and
    // accepted on Continue / Enter). Only valid while the suggestion still starts with the current input.
    val suggestionSuffix = state.accountProviderSuggestion
        ?.takeIf { it.length > input.length && it.startsWith(input, ignoreCase = true) }
        ?.substring(input.length)
        .orEmpty()

    // The Continue button lives in a footer slot that is not recomposed while the user types, so a direct
    // capture of the field text (via ::submit) goes stale and can submit an empty/prefilled value. Reading
    // the value through rememberUpdatedState keeps it current regardless of which composition scope reads it.
    val accountProviderToSubmit by rememberUpdatedState(input + suggestionSuffix)

    fun submit() {
        // Dismiss the keyboard and release focus while the account provider is being validated.
        focusManager.clearFocus(force = true)
        // Submit the exact field text (plus any accepted suggestion).
        eventSink(ConfirmAccountProviderEvents.Continue(accountProviderToSubmit))
    }

    // Once a validation / login error has been dismissed, return focus and the keyboard to the field so
    // the user can correct the account provider and retry.
    var wasShowingError by remember { mutableStateOf(false) }
    LaunchedEffect(state.isShowingError) {
        if (wasShowingError && !state.isShowingError) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        wasShowingError = state.isShowingError
    }

    HeaderFooterPage(
        modifier = modifier,
        header = {
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(top = 60.dp),
                iconStyle = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
                title = stringResource(
                    id = if (state.isAccountCreation) {
                        R.string.screen_change_server_title_register
                    } else {
                        R.string.screen_change_server_title_login
                    }
                ),
                subTitle = null,
            )
        },
        footer = {
            ButtonColumnMolecule {
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    showProgress = state.isLoading,
                    onClick = ::submit,
                    enabled = state.submitEnabled && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginContinue)
                )
            }
        }
    ) {
        val ghostColor = ElementTheme.colors.textSecondary
        val ghostTransformation = remember(suggestionSuffix, ghostColor) {
            GhostSuffixVisualTransformation(suggestionSuffix, ghostColor)
        }
        TextField(
            value = input,
            onValueChange = {
                input = it
                eventSink(ConfirmAccountProviderEvents.UserInputChanged(it))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .focusRequester(focusRequester)
                .testTag(TestTags.changeServerServer),
            label = stringResource(id = R.string.screen_change_server_textfield_header),
            placeholder = stringResource(id = R.string.screen_change_server_textfield_placeholder),
            supportingText = stringResource(
                id = if (state.isAccountCreation) {
                    R.string.screen_change_server_textfield_footer_register
                } else {
                    R.string.screen_change_server_textfield_footer_login
                }
            ),
            visualTransformation = ghostTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (suggestionSuffix.isNotEmpty()) {
                        // A completion is showing: accept it into the field and drop focus, dismissing the
                        // keyboard. This shows the completed server (with no stray mid-text cursor, since the
                        // field is no longer focused) ready to sign in with Continue. When no completion is
                        // showing, Done submits directly.
                        val accepted = input + suggestionSuffix
                        input = accepted
                        eventSink(ConfirmAccountProviderEvents.UserInputChanged(accepted))
                        focusManager.clearFocus(force = true)
                    } else {
                        submit()
                    }
                }
            ),
            singleLine = true,
            trailingIcon = if (input.isNotEmpty()) {
                {
                    Box(
                        Modifier.clickable(
                            onClickLabel = stringResource(CommonStrings.action_clear),
                            role = Role.Button,
                        ) {
                            input = ""
                            eventSink(ConfirmAccountProviderEvents.UserInputChanged(""))
                        }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.Close(),
                            contentDescription = stringResource(CommonStrings.action_clear)
                        )
                    }
                }
            } else {
                null
            },
        )
    }

    // Renders the account-provider validation errors, progress and permission dialogs.
    // The successful validation itself is observed by the presenter, which then proceeds with the login flow.
    ChangeServerView(
        state = state.changeServerState,
        onLearnMoreClick = onLearnMoreClick,
        onSuccess = {},
    )

    LoginModeView(
        loginMode = state.loginModeState.loginMode,
        onClearError = { eventSink(ConfirmAccountProviderEvents.ClearError) },
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

/**
 * Appends [suffix] (in [color]) after the user's text as a non-editable inline autocomplete hint.
 * The underlying field value stays limited to what the user actually typed, so the cursor can never
 * enter the suggested region.
 */
private class GhostSuffixVisualTransformation(
    private val suffix: String,
    private val color: Color,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (suffix.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val transformed = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = color)) { append(suffix) }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset
            override fun transformedToOriginal(offset: Int) = offset.coerceAtMost(text.length)
        }
        return TransformedText(transformed, offsetMapping)
    }
}

@PreviewsDayNight
@Composable
internal fun ConfirmAccountProviderViewPreview(
    @PreviewParameter(ConfirmAccountProviderStatePreviewParam::class) state: ConfirmAccountProviderState
) = ElementPreview {
    ConfirmAccountProviderView(
        state = state,
        onOAuthDetails = {},
        onNeedLoginPassword = {},
        onLearnMoreClick = {},
    )
}
