/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.check

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.modifiers.bringIntoViewOnImeVisible
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Asks for the recovery key the user already has, to make sure that it is correct.
 *
 * Ref: https://www.figma.com/design/hDUT4q0pynhkbmB5s56GN8/ER-162--Recovery-Key-Usability---Management?node-id=1720-62480&t=1MdCyUpRaboz2aQy-4
 *
 * @param state the recovery key being entered and whether it is correct.
 * @param doneButtonText the string resource of the button that finishes the screen once the key is known to be correct.
 * @param onDone called when the user taps the done button.
 * @param onBackClick called when the user navigates back; not available once the key is confirmed.
 * @param modifier the modifier to apply to the screen.
 */
@Composable
fun SecureBackupCheckKeyView(
    state: SecureBackupCheckKeyState,
    doneButtonText: Int,
    onDone: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The key is confirmed, so there is nothing to go back to.
    BackHandler(enabled = state.isCompleted) {}

    FlowStepPage(
        modifier = modifier,
        isScrollable = true,
        onBackClick = onBackClick.takeUnless { state.isCompleted },
        title = stringResource(id = if (state.isCompleted) R.string.screen_recovery_key_check_success_title else R.string.screen_recovery_key_check_title),
        subTitle = stringResource(
            id = if (state.isCompleted) R.string.screen_custom_recovery_key_success_subtitle else R.string.screen_recovery_key_check_subtitle
        ),
        iconStyle = if (state.isCompleted) BigIcon.Style.SuccessSolid else BigIcon.Style.Default(CompoundIcons.KeySolid()),
        buttons = { Buttons(state, doneButtonText, onDone) },
    ) {
        if (!state.isCompleted) {
            Content(state)
        }
    }
}

@Composable
private fun Content(state: SecureBackupCheckKeyState) {
    RecoveryKeyView(
        modifier = Modifier
            .bringIntoViewOnImeVisible()
            .padding(top = 52.dp, bottom = 32.dp),
        state = state.recoveryKeyViewState,
        onClick = null,
        onChange = { state.eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange(it)) },
        onSubmit = { state.eventSink(SecureBackupCheckKeyEvent.Submit) },
        toggleRecoveryKeyVisibility = { state.eventSink(SecureBackupCheckKeyEvent.ChangeRecoveryKeyFieldContentsVisibility(it)) },
        label = stringResource(id = R.string.screen_recovery_key_check_key_title),
        errorText = stringResource(id = R.string.screen_recovery_key_confirm_error_title).takeIf { state.isKeyIncorrect },
        hideFooter = true,
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupCheckKeyState,
    doneButtonText: Int,
    onDone: () -> Unit,
) {
    if (state.isCompleted) {
        Button(
            text = stringResource(id = doneButtonText),
            modifier = Modifier.fillMaxWidth(),
            onClick = onDone,
        )
    } else {
        Button(
            text = stringResource(id = CommonStrings.action_continue),
            enabled = state.isSubmitEnabled,
            showProgress = state.recoveryKeyViewState.inProgress,
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.eventSink(SecureBackupCheckKeyEvent.Submit) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupCheckKeyViewPreview(
    @PreviewParameter(SecureBackupCheckKeyStatePreviewParam::class) state: SecureBackupCheckKeyState
) = ElementPreview {
    SecureBackupCheckKeyView(
        state = state,
        doneButtonText = CommonStrings.action_done,
        onDone = {},
        onBackClick = {},
    )
}
