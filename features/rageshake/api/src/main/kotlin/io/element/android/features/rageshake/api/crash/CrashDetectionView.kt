/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.crash

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.rageshake.api.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun CrashDetectionView(
    state: CrashDetectionState,
    onOpenBugReport: () -> Unit = { },
) {
    fun onPopupDismissed() {
        state.eventSink(CrashDetectionEvents.ResetAllCrashData)
    }

    if (state.crashDetected) {
        CrashDetectionContent(
            appName = state.appName,
            onYesClick = onOpenBugReport,
            onNoClick = ::onPopupDismissed,
            onDismiss = ::onPopupDismissed,
        )
    }
}

@Composable
private fun CrashDetectionContent(
    appName: String,
    onNoClick: () -> Unit = { },
    onYesClick: () -> Unit = { },
    onDismiss: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.action_report_bug),
        content = stringResource(id = R.string.crash_detection_dialog_content, appName),
        submitText = stringResource(id = CommonStrings.action_yes),
        cancelText = stringResource(id = CommonStrings.action_no),
        onCancelClick = onNoClick,
        onSubmitClick = onYesClick,
        onDismiss = onDismiss,
    )
}

@PreviewsDayNight
@Composable
internal fun CrashDetectionViewPreview() = ElementPreview {
    CrashDetectionView(
        state = aCrashDetectionState().copy(crashDetected = true)
    )
}
