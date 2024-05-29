/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
