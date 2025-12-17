/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.deactivation.impl.R
import io.element.android.features.logout.impl.ui.AccountDeactivationActionDialog
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.components.list.SwitchListItem
import io.element.android.libraries.designsystem.modifiers.onTabOrEnterKeyFocusNext
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDeactivationView(
    state: AccountDeactivationState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                titleStr = stringResource(R.string.screen_deactivate_account_title),
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .imePadding()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(state = scrollState)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Content(
                state = state,
                onSubmitClick = {
                    eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Buttons(
                state = state,
                onSubmitClick = {
                    eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
                }
            )
        }
    }
    AccountDeactivationActionDialog(
        state.accountDeactivationAction,
        onConfirmClick = {
            eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
        },
        onRetryClick = {
            eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = true))
        },
        onDismissDialog = {
            eventSink(AccountDeactivationEvents.CloseDialogs)
        },
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: AccountDeactivationState,
    onSubmitClick: () -> Unit,
) {
    val logoutAction = state.accountDeactivationAction
    Button(
        text = stringResource(CommonStrings.action_deactivate),
        showProgress = logoutAction is AsyncAction.Loading,
        destructive = true,
        enabled = state.submitEnabled,
        modifier = Modifier.fillMaxWidth(),
        onClick = onSubmitClick,
    )
}

@Composable
private fun Content(
    state: AccountDeactivationState,
    onSubmitClick: () -> Unit,
) {
    val isLoading by remember(state.deactivateFormState) {
        derivedStateOf {
            state.accountDeactivationAction is AsyncAction.Loading
        }
    }
    val eraseData = state.deactivateFormState.eraseData
    var passwordFieldState by textFieldState(stateValue = state.deactivateFormState.password)

    val focusManager = LocalFocusManager.current
    val eventSink = state.eventSink

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = buildAnnotatedStringWithStyledPart(
                R.string.screen_deactivate_account_description,
                R.string.screen_deactivate_account_description_bold_part,
                color = ElementTheme.colors.textSecondary,
                bold = true,
                underline = false,
            ),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
        )
        InfoListOrganism(
            items = persistentListOf(
                InfoListItem(
                    message = buildAnnotatedStringWithStyledPart(
                        R.string.screen_deactivate_account_list_item_1,
                        R.string.screen_deactivate_account_list_item_1_bold_part,
                        color = ElementTheme.colors.textSecondary,
                        bold = true,
                        underline = false,
                    ),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconCriticalPrimary,
                        )
                    },
                ),
                InfoListItem(
                    message = stringResource(R.string.screen_deactivate_account_list_item_2),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconCriticalPrimary,
                        )
                    },
                ),
                InfoListItem(
                    message = stringResource(R.string.screen_deactivate_account_list_item_3),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconCriticalPrimary,
                        )
                    },
                ),
                InfoListItem(
                    message = stringResource(R.string.screen_deactivate_account_list_item_4),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Check(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconSuccessPrimary,
                        )
                    },
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyMdRegular,
            textColor = ElementTheme.colors.textSecondary,
            iconTint = ElementTheme.colors.iconSuccessPrimary,
            backgroundColor = Color.Transparent,
        )

        Column {
            SwitchListItem(
                headline = stringResource(R.string.screen_deactivate_account_delete_all_messages),
                value = eraseData,
                onChange = {
                    eventSink(AccountDeactivationEvents.SetEraseData(it))
                },
                enabled = !isLoading,
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(R.string.screen_deactivate_account_delete_all_messages_notice),
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            var passwordVisible by remember { mutableStateOf(false) }
            if (isLoading) {
                // Ensure password is hidden when user submits the form
                passwordVisible = false
            }
            TextField(
                value = passwordFieldState,
                label = stringResource(CommonStrings.action_confirm_password),
                readOnly = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .onTabOrEnterKeyFocusNext(focusManager)
                    .testTag(TestTags.loginPassword)
                    .semantics {
                        contentType = ContentType.Password
                    },
                onValueChange = {
                    val sanitized = it.sanitize()
                    passwordFieldState = sanitized
                    eventSink(AccountDeactivationEvents.SetPassword(sanitized))
                },
                placeholder = stringResource(CommonStrings.common_password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image =
                        if (passwordVisible) CompoundIcons.VisibilityOn() else CompoundIcons.VisibilityOff()
                    val description =
                        if (passwordVisible) stringResource(CommonStrings.a11y_hide_password) else stringResource(CommonStrings.a11y_show_password)

                    Box(modifier = Modifier.clickable { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmitClick() }
                ),
                singleLine = true,
            )
        }
    }
}

/**
 * Ensure that the string does not contain any new line characters, which can happen when pasting values.
 */
private fun String.sanitize(): String {
    return replace("\n", "")
}

@PreviewsDayNight
@Composable
internal fun AccountDeactivationViewPreview(
    @PreviewParameter(AccountDeactivationStateProvider::class) state: AccountDeactivationState,
) = ElementPreview {
    AccountDeactivationView(
        state,
        onBackClick = {},
    )
}
