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

package io.element.android.features.rageshake.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun CrashDetectionView(
    state: CrashDetectionState,
    onOpenBugReport: () -> Unit = { },
) {
    LogCompositions(
        tag = "Crash",
        msg = "CrashDetectionScreen"
    )

    fun onPopupDismissed() {
        state.eventSink(CrashDetectionEvents.ResetAllCrashData)
    }

    if (state.crashDetected) {
        CrashDetectionContent(
            state,
            onYesClicked = onOpenBugReport,
            onNoClicked = ::onPopupDismissed,
            onDismiss = ::onPopupDismissed,
        )
    }
}

@Composable
fun CrashDetectionContent(
    state: CrashDetectionState,
    onNoClicked: () -> Unit = { },
    onYesClicked: () -> Unit = { },
    onDismiss: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = StringR.string.send_bug_report),
        content = stringResource(id = StringR.string.send_bug_report_app_crashed),
        submitText = stringResource(id = StringR.string.yes),
        cancelText = stringResource(id = StringR.string.no),
        onCancelClicked = onNoClicked,
        onSubmitClicked = onYesClicked,
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
fun CrashDetectionContentLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun CrashDetectionContentDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    CrashDetectionContent(
        state = aCrashDetectionState()
    )
}
