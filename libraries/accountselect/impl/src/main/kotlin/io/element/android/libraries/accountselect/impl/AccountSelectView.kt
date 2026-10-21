/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.components.MatrixUserRow

/**
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=1294-7832
 */
@Suppress("MultipleEmitters") // False positive
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
                title = {},
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
            SelectAccountHeader(state)
            LazyColumn {
                item {
                    HorizontalDivider()
                }
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

@Composable
private fun SelectAccountHeader(state: AccountSelectState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BigIcon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select an account to open the link:",
            modifier = Modifier
                .semantics {
                    heading()
                },
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
        )
        if (state.linkPillText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            LinkPill(
                text = state.linkPillText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LinkPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(ElementTheme.colors.bgBadgePrimary)
            .padding(horizontal = 6.dp),
    ) {
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textOnSolidPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AccountSelectViewPreview(@PreviewParameter(AccountSelectStatePreviewParam::class) state: AccountSelectState) = ElementPreview {
    AccountSelectView(
        state = state,
        onSelectAccount = {},
        onDismiss = {},
    )
}
