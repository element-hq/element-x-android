/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings

@Suppress("MultipleEmitters") // False positive
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectView(
    state: AccountSelectState,
    onSelectAccount: (SessionId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = { onDismiss() })
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.common_select_account),
                navigationIcon = {
                    BackButton(onClick = { onDismiss() })
                },
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            LazyColumn {
                items(state.accounts, key = { it.userId }) { matrixUser ->
                    Column {
                        MatrixUserRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAccount(matrixUser.userId)
                                }
                                .padding(vertical = 8.dp),
                            matrixUser = matrixUser,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AccountSelectViewPreview(@PreviewParameter(AccountSelectStateProvider::class) state: AccountSelectState) = ElementPreview {
    AccountSelectView(
        state = state,
        onSelectAccount = {},
        onDismiss = {},
    )
}
