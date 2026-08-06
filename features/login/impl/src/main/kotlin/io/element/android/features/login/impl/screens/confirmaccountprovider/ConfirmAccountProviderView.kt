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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import io.element.android.features.login.impl.changeserver.ChangeServerView
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.modifiers.onTabOrEnterKeyFocusNext
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

@Composable
fun ConfirmAccountProviderView(
    state: ConfirmAccountProviderState,
    onOAuthDetails: (OAuthDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    HeaderFooterPage(
        modifier = modifier,
        header = {
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(top = 60.dp),
                iconStyle = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
                title = stringResource(
                    id = if (state.isAccountCreation) {
                        CommonStrings.screen_select_server_title_register
                    } else {
                        CommonStrings.screen_select_server_title_login
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
                    onClick = { eventSink(ConfirmAccountProviderEvents.Continue) },
                    enabled = state.submitEnabled && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginContinue)
                )
            }
        }
    ) {
        var input by textFieldState(stateValue = state.accountProviderInput)
        val focusManager = LocalFocusManager.current
        // Inline autocomplete: render the un-typed remainder of the suggested account provider in grey.
        val suggestionSuffix = state.accountProviderSuggestion
            ?.takeIf { it.length > input.length && it.startsWith(input, ignoreCase = true) }
            ?.substring(input.length)
            .orEmpty()
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
                .onTabOrEnterKeyFocusNext(focusManager)
                .testTag(TestTags.changeServerServer),
            label = stringResource(id = CommonStrings.screen_select_server_textfield_header),
            placeholder = stringResource(id = CommonStrings.screen_select_server_textfield_placeholder),
            supportingText = stringResource(
                id = if (state.isAccountCreation) {
                    CommonStrings.screen_select_server_textfield_footer_register
                } else {
                    CommonStrings.screen_select_server_textfield_footer_login
                }
            ),
            visualTransformation = ghostTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                eventSink(ConfirmAccountProviderEvents.Continue)
            }),
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
        onCreateAccountContinue = onCreateAccountContinue,
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
    @PreviewParameter(ConfirmAccountProviderStateProvider::class) state: ConfirmAccountProviderState
) = ElementPreview {
    ConfirmAccountProviderView(
        state = state,
        onOAuthDetails = {},
        onNeedLoginPassword = {},
        onCreateAccountContinue = {},
        onLearnMoreClick = {},
    )
}
