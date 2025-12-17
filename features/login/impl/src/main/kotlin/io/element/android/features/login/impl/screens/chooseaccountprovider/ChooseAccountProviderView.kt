/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.AccountProviderView
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ChooseAccountProviderView(
    state: ChooseAccountProviderState,
    onBackClick: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf {
            state.loginMode is AsyncData.Loading
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onBackClick) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
            ) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                    iconStyle = BigIcon.Style.Default(CompoundIcons.HomeSolid()),
                    title = stringResource(id = R.string.screen_server_confirmation_title_picker_mode),
                    subTitle = null,
                )

                state.accountProviders.forEach { item ->
                    val alteredItem = if (item.isMatrixOrg) {
                        // Set the subtitle from the resource
                        item.copy(
                            subtitle = stringResource(id = R.string.screen_change_account_provider_matrix_org_subtitle),
                        )
                    } else {
                        item
                    }
                    AccountProviderView(
                        item = alteredItem,
                        selected = item == state.selectedAccountProvider,
                        onClick = {
                            state.eventSink(ChooseAccountProviderEvents.SelectAccountProvider(item))
                        }
                    )
                }
                Spacer(Modifier.height(32.dp))
                // Flexible spacing to keep the submit button at the bottom
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    text = stringResource(id = CommonStrings.action_continue),
                    showProgress = isLoading,
                    onClick = {
                        state.eventSink(ChooseAccountProviderEvents.Continue)
                    },
                    enabled = state.submitEnabled || isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
            LoginModeView(
                loginMode = state.loginMode,
                onClearError = {
                    state.eventSink(ChooseAccountProviderEvents.ClearError)
                },
                onLearnMoreClick = onLearnMoreClick,
                onOidcDetails = onOidcDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onCreateAccountContinue = onCreateAccountContinue,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ChooseAccountProviderViewPreview(@PreviewParameter(ChooseAccountProviderStateProvider::class) state: ChooseAccountProviderState) = ElementPreview {
    ChooseAccountProviderView(
        state = state,
        onBackClick = { },
        onLearnMoreClick = { },
        onOidcDetails = { },
        onNeedLoginPassword = { },
        onCreateAccountContinue = { },
    )
}
