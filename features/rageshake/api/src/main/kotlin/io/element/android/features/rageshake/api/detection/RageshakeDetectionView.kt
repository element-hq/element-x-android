/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import io.element.android.features.rageshake.api.R
import io.element.android.features.rageshake.api.screenshot.ImageResult
import io.element.android.features.rageshake.api.screenshot.screenshot
import io.element.android.libraries.androidutils.hardware.vibrate
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RageshakeDetectionView(
    state: RageshakeDetectionState,
    onOpenBugReport: () -> Unit = { },
) {
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
            onScreenshot = { eventSink(RageshakeDetectionEvents.ProcessScreenshot(it)) }
        )
        state.showDialog -> {
            LaunchedEffect(Unit) {
                context.vibrate()
            }
            RageshakeDialogContent(
                onNoClick = { eventSink(RageshakeDetectionEvents.Dismiss) },
                onDisableClick = { eventSink(RageshakeDetectionEvents.Disable) },
                onYesClick = onOpenBugReport
            )
        }
    }
}

@Composable
private fun TakeScreenshot(
    onScreenshot: (ImageResult) -> Unit
) {
    val view = LocalView.current
    val latestOnScreenshot by rememberUpdatedState(onScreenshot)
    LaunchedEffect(Unit) {
        view.screenshot {
            latestOnScreenshot(it)
        }
    }
}

@Composable
private fun RageshakeDialogContent(
    onNoClick: () -> Unit = { },
    onDisableClick: () -> Unit = { },
    onYesClick: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.action_report_bug),
        content = stringResource(id = R.string.rageshake_detection_dialog_content),
        thirdButtonText = stringResource(id = CommonStrings.action_disable),
        submitText = stringResource(id = CommonStrings.action_yes),
        cancelText = stringResource(id = CommonStrings.action_no),
        onCancelClick = onNoClick,
        onThirdButtonClick = onDisableClick,
        onSubmitClick = onYesClick,
        onDismiss = onNoClick,
    )
}

@PreviewsDayNight
@Composable
internal fun RageshakeDialogContentPreview() = ElementPreview {
    RageshakeDialogContent()
}
