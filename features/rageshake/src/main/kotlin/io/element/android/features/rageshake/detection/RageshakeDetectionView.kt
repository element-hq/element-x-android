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

package io.element.android.features.rageshake.detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import io.element.android.features.rageshake.screenshot.ImageResult
import io.element.android.features.rageshake.screenshot.screenshot
import io.element.android.libraries.androidutils.hardware.vibrate
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewDefaults
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun RageshakeDetectionView(
    state: RageshakeDetectionState,
    onOpenBugReport: () -> Unit = { },
) {
    LogCompositions(
        tag = "Rageshake",
        msg = "RageshakeDetectionScreen"
    )
    val eventSink = state.eventSink
    val context = LocalContext.current
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> eventSink(RageshakeDetectionEvents.StartDetection)
            Lifecycle.Event.ON_PAUSE -> eventSink(RageshakeDetectionEvents.StopDetection)
            else -> Unit
        }
    }
    when {
        state.takeScreenshot -> TakeScreenshot(
            onScreenshotTaken = { eventSink(RageshakeDetectionEvents.ProcessScreenshot(it)) }
        )
        state.showDialog -> {
            LaunchedEffect(Unit) {
                context.vibrate()
            }
            RageshakeDialogContent(
                onNoClicked = { eventSink(RageshakeDetectionEvents.Dismiss) },
                onDisableClicked = { eventSink(RageshakeDetectionEvents.Disable) },
                onYesClicked = onOpenBugReport
            )
        }
    }
}

@Composable
private fun TakeScreenshot(
    onScreenshotTaken: (ImageResult) -> Unit = {}
) {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        view.screenshot {
            onScreenshotTaken(it)
        }
    }
}

@Composable
fun RageshakeDialogContent(
    onNoClicked: () -> Unit = { },
    onDisableClicked: () -> Unit = { },
    onYesClicked: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = StringR.string.send_bug_report),
        content = stringResource(id = StringR.string.send_bug_report_alert_message),
        thirdButtonText = stringResource(id = StringR.string.action_disable),
        submitText = stringResource(id = StringR.string.yes),
        cancelText = stringResource(id = StringR.string.no),
        onCancelClicked = onNoClicked,
        onThirdButtonClicked = onDisableClicked,
        onSubmitClicked = onYesClicked,
        onDismiss = onNoClicked,
    )
}

@PreviewDefaults
@Composable
internal fun RageshakeDialogContentPreview() = ElementPreview {
    RageshakeDialogContent()
}
