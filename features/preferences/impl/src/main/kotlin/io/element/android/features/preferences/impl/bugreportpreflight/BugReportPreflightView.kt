/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.bugreportpreflight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun BugReportPreflightView(
    state: BugReportPreflightUiState,
    onBackClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = R.string.screen_bug_report_preflight_title),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(id = R.string.screen_bug_report_preflight_description),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Start,
        )
        when (state) {
            BugReportPreflightUiState.Loading -> {
                AsyncLoading()
            }
            is BugReportPreflightUiState.Content -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = ElementTheme.colors.bgSubtleSecondary,
                    border = BorderStroke(1.dp, ElementTheme.colors.borderInteractiveSecondary),
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = state.reportText,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = CommonStrings.action_copy),
                        onClick = { onCopyClick(state.reportText) },
                    )
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = CommonStrings.action_share),
                        onClick = { onShareClick(state.reportText) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
