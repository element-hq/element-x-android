package io.element.android.x.features.rageshake.detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.core.compose.OnLifecycleEvent
import io.element.android.x.core.hardware.vibrate
import io.element.android.x.core.screenshot.ImageResult
import io.element.android.x.core.screenshot.screenshot
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.x.element.resources.R as ElementR

@Composable
fun RageshakeDetectionScreen(
    viewModel: RageshakeDetectionViewModel = mavericksViewModel(),
    onOpenBugReport: () -> Unit = { },
) {
    val state: RageshakeDetectionViewState by viewModel.collectAsState()
    LogCompositions(tag = "Rageshake", msg = "RageshakeDetectionScreen")
    val context = LocalContext.current
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> viewModel.start()
            Lifecycle.Event.ON_PAUSE -> viewModel.stop()
            else -> Unit
        }
    }

    when {
        state.takeScreenshot -> TakeScreenshot(
            onScreenshotTaken = viewModel::onScreenshotTaken
        )
        state.showDialog -> {
            LaunchedEffect(key1 = "RS_diag") {
                context.vibrate()
            }
            RageshakeDialogContent(
                state,
                onNoClicked = viewModel::onNo,
                onDisableClicked = {
                    viewModel.onEnableClicked(false)
                },
                onYesClicked = {
                    onOpenBugReport()
                    viewModel.onYes()
                }
            )
        }
    }
}

@Composable
private fun TakeScreenshot(
    onScreenshotTaken: (ImageResult) -> Unit = {}
) {
    val view = LocalView.current
    view.screenshot {
        onScreenshotTaken(it)
    }
}

@Composable
fun RageshakeDialogContent(
    state: RageshakeDetectionViewState,
    onNoClicked: () -> Unit = { },
    onDisableClicked: () -> Unit = { },
    onYesClicked: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = ElementR.string.send_bug_report),
        content = stringResource(id = ElementR.string.send_bug_report_alert_message),
        thirdButtonText = stringResource(id = ElementR.string.action_disable),
        submitText = stringResource(id = ElementR.string.yes),
        cancelText = stringResource(id = ElementR.string.no),
        onThirdButtonClicked = onDisableClicked,
        onSubmitClicked = onYesClicked,
        onDismiss = onNoClicked,
    )
}

@Preview
@Composable
fun RageshakeDialogContentPreview() {
    ElementXTheme {
        RageshakeDialogContent(
            state = RageshakeDetectionViewState()
        )
    }
}
