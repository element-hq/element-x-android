/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyView
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.exception.isNetworkError
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Shows a new recovery key to save, which only becomes active once the user confirms that they have saved it.
 *
 * Ref: https://www.figma.com/design/hDUT4q0pynhkbmB5s56GN8/ER-162--Recovery-Key-Usability---Management?node-id=156-27104&t=1MdCyUpRaboz2aQy-4
 *
 * @param state the key to save and the progress of activating it.
 * @param doneButtonText the string resource of the button that finishes the screen once the new key is active.
 * @param onDone called when the user taps the done button.
 * @param onBackClick called when the user navigates back; not available once the new key is active.
 * @param modifier the modifier to apply to the screen.
 */
@Composable
fun SecureBackupGenerateKeyView(
    state: SecureBackupGenerateKeyState,
    doneButtonText: Int,
    onDone: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The new key is active, so there is nothing to go back to.
    BackHandler(enabled = state.isCompleted) {}

    FlowStepPage(
        modifier = modifier,
        isScrollable = true,
        onBackClick = onBackClick.takeUnless { state.isCompleted },
        title = title(state),
        subTitle = subtitle(state),
        iconStyle = if (state.isCompleted) BigIcon.Style.SuccessSolid else BigIcon.Style.Default(CompoundIcons.KeySolid()),
        buttons = { Buttons(state, doneButtonText, onDone) },
    ) {
        Content(state)
    }

    if (state.showSaveConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_recovery_key_setup_confirmation_title),
            content = stringResource(id = R.string.screen_recovery_key_setup_confirmation_description),
            submitText = stringResource(id = CommonStrings.action_continue),
            onSubmitClick = { state.eventSink(SecureBackupGenerateKeyEvent.ConfirmSaved) },
            onDismiss = { state.eventSink(SecureBackupGenerateKeyEvent.DismissConfirmation) },
        )
    }

    when (val action = state.activateAction) {
        AsyncAction.Loading -> ProgressDialog()
        is AsyncAction.Failure -> ErrorDialog(
            title = stringResource(id = CommonStrings.common_something_went_wrong),
            content = stringResource(
                id = if (action.error.isNetworkError()) {
                    CommonStrings.common_please_check_internet_connection
                } else {
                    CommonStrings.common_something_went_wrong_message
                }
            ),
            onSubmit = { state.eventSink(SecureBackupGenerateKeyEvent.DismissError) },
        )
        else -> Unit
    }
}

@Composable
private fun title(state: SecureBackupGenerateKeyState): String {
    return stringResource(
        id = when {
            !state.isCompleted -> R.string.screen_recovery_key_save_title
            state.isReplacingExistingKey -> R.string.screen_recovery_key_success_title
            else -> R.string.screen_custom_recovery_key_success_title
        }
    )
}

@Composable
private fun subtitle(state: SecureBackupGenerateKeyState): String {
    return stringResource(
        id = if (state.isCompleted) R.string.screen_custom_recovery_key_success_subtitle else R.string.screen_recovery_key_save_description
    )
}

@Composable
private fun Content(state: SecureBackupGenerateKeyState) {
    when {
        !state.isCompleted -> RecoveryKeyView(
            modifier = Modifier.padding(top = 52.dp),
            state = RecoveryKeyViewState(
                recoveryKeyUserStory = if (state.isReplacingExistingKey) RecoveryKeyUserStory.Change else RecoveryKeyUserStory.Setup,
                formattedRecoveryKey = state.recoveryKey,
                displayTextFieldContents = true,
                inProgress = false,
            ),
            onClick = { state.eventSink(SecureBackupGenerateKeyEvent.CopyRecoveryKey) },
            onChange = null,
            onSubmit = null,
            toggleRecoveryKeyVisibility = {},
            footerText = stringResource(id = R.string.screen_recovery_key_setup_generate_key_description),
        )
        // Where to change the key later is only worth saying when this is the first one.
        !state.isReplacingExistingKey -> Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            text = stringResource(id = R.string.screen_custom_recovery_key_success_notice),
            textAlign = TextAlign.Center,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdRegular,
        )
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupGenerateKeyState,
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
        val context = LocalContext.current
        val chooserTitle = stringResource(id = R.string.screen_recovery_key_save_action)
        OutlinedButton(
            text = stringResource(id = R.string.screen_recovery_key_save_action),
            leadingIcon = IconSource.Vector(CompoundIcons.Download()),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                context.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = chooserTitle,
                    text = state.recoveryKey,
                )
            },
        )
        Button(
            text = stringResource(id = CommonStrings.action_continue),
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.eventSink(SecureBackupGenerateKeyEvent.Continue) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupGenerateKeyViewPreview(
    @PreviewParameter(SecureBackupGenerateKeyStatePreviewParam::class) state: SecureBackupGenerateKeyState
) = ElementPreview {
    SecureBackupGenerateKeyView(
        state = state,
        doneButtonText = CommonStrings.action_done,
        onDone = {},
        onBackClick = {},
    )
}
