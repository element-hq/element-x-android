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

package io.element.android.x.features.rageshake.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.x.element.resources.R as ElementR

@Composable
fun CrashDetectionScreen(
    viewModel: CrashDetectionViewModel = mavericksViewModel(),
    onOpenBugReport: () -> Unit = { },
) {
    val state: CrashDetectionViewState by viewModel.collectAsState()
    LogCompositions(tag = "Crash", msg = "CrashDetectionScreen")

    if (state.crashDetected) {
        CrashDetectionContent(
            state,
            onYesClicked = {
                viewModel.onYes()
                onOpenBugReport()
            },
            onNoClicked = viewModel::onPopupDismissed,
            onDismiss = viewModel::onPopupDismissed,
        )
    }
}

@Composable
fun CrashDetectionContent(
    state: CrashDetectionViewState,
    onNoClicked: () -> Unit = { },
    onYesClicked: () -> Unit = { },
    onDismiss: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = ElementR.string.send_bug_report),
        content = stringResource(id = ElementR.string.send_bug_report_app_crashed),
        submitText = stringResource(id = ElementR.string.yes),
        cancelText = stringResource(id = ElementR.string.no),
        onCancelClicked = onNoClicked,
        onSubmitClicked = onYesClicked,
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
fun CrashDetectionContentPreview() {
    ElementXTheme {
        CrashDetectionContent(
            state = CrashDetectionViewState()
        )
    }
}
