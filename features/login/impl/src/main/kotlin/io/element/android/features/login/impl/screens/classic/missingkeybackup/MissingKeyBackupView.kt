/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.missingkeybackup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.NumberedListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MissingKeyBackupView(
    state: MissingKeyBackupState,
    onBackClick: () -> Unit,
    onOpenClassicClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClick = onBackClick,
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        title = stringResource(id = R.string.screen_missing_key_backup_title, state.appName),
        content = { Content(state) },
        buttons = {
            Buttons(
                onOpenClassicClick = onOpenClassicClick,
            )
        }
    )
}

@Composable
private fun Content(
    state: MissingKeyBackupState,
) {
    NumberedListOrganism(
        modifier = Modifier.padding(top = 50.dp, start = 20.dp, end = 20.dp),
        items = persistentListOf(
            AnnotatedString(stringResource(R.string.screen_missing_key_backup_step_1)),
            AnnotatedString(stringResource(R.string.screen_missing_key_backup_step_2_android)),
            AnnotatedString(stringResource(R.string.screen_missing_key_backup_step_3_android)),
            AnnotatedString(stringResource(R.string.screen_missing_key_backup_step_4)),
            AnnotatedString(stringResource(R.string.screen_missing_key_backup_step_5, state.appName)),
        ),
    )
}

@Composable
private fun ColumnScope.Buttons(
    onOpenClassicClick: () -> Unit,
) {
    Button(
        text = stringResource(id = R.string.screen_missing_key_backup_open_element_classic),
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenClassicClick,
    )
}

@PreviewsDayNight
@Composable
internal fun MissingKeyBackupViewPreview(@PreviewParameter(MissingKeyBackupStateProvider::class) state: MissingKeyBackupState) = ElementPreview {
    MissingKeyBackupView(
        state = state,
        onBackClick = {},
        onOpenClassicClick = {},
    )
}
