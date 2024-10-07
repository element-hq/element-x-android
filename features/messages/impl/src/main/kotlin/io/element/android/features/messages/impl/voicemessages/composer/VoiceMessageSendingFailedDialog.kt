/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun VoiceMessageSendingFailedDialog(
    onDismiss: () -> Unit,
) {
    ErrorDialog(
        title = stringResource(CommonStrings.common_error),
        content = stringResource(CommonStrings.error_failed_uploading_voice_message),
        onSubmit = onDismiss,
        submitText = stringResource(CommonStrings.action_ok),
    )
}
