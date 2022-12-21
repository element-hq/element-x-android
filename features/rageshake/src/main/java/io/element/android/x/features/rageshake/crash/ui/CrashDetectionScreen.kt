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
