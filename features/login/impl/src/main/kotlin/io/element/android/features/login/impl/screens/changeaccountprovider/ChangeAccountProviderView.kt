/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.changeaccountprovider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderView
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerView
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=604-60817
 */
@Composable
fun ChangeAccountProviderView(
    state: ChangeAccountProviderState,
    onBackClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onSuccess: () -> Unit,
    onOtherProviderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    .verticalScroll(state = rememberScrollState())
            ) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                    iconStyle = BigIcon.Style.Default(Icons.Filled.Home),
                    title = stringResource(id = R.string.screen_change_account_provider_title),
                    subTitle = stringResource(id = R.string.screen_change_account_provider_subtitle),
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
                        onClick = {
                            state.changeServerState.eventSink.invoke(ChangeServerEvents.ChangeServer(alteredItem))
                        }
                    )
                }
                // Other
                AccountProviderView(
                    item = AccountProvider(
                        url = "",
                        title = stringResource(id = R.string.screen_change_account_provider_other),
                    ),
                    onClick = onOtherProviderClick
                )
                Spacer(Modifier.height(32.dp))
            }
            ChangeServerView(
                state = state.changeServerState,
                onLearnMoreClick = onLearnMoreClick,
                onSuccess = onSuccess,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ChangeAccountProviderViewPreview(@PreviewParameter(ChangeAccountProviderStateProvider::class) state: ChangeAccountProviderState) = ElementPreview {
    ChangeAccountProviderView(
        state = state,
        onBackClick = { },
        onLearnMoreClick = { },
        onSuccess = { },
        onOtherProviderClick = { },
    )
}
