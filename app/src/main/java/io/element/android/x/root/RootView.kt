package io.element.android.x.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.airbnb.android.showkase.models.Showkase
import io.element.android.x.component.ShowkaseButton
import io.element.android.x.core.screenshot.ImageResult
import io.element.android.x.features.rageshake.crash.ui.CrashDetectionView
import io.element.android.x.features.rageshake.detection.RageshakeDetectionView
import io.element.android.x.getBrowserIntent

@Composable
fun RootView(
    state: RootState,
    modifier: Modifier = Modifier,
    onHideShowkaseClicked: () -> Unit = { },
    onOpenBugReport: () -> Unit = {},
    onCrashDetectedDismissed: () -> Unit = {},
    onDisableRageshake: () -> Unit = {},
    onDismissRageshake: () -> Unit = {},
    onScreenshotTaken: (ImageResult) -> Unit = {},
    children: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        children()
        val context = LocalContext.current
        ShowkaseButton(
            isVisible = state.isShowkaseButtonVisible,
            onCloseClicked = onHideShowkaseClicked,
            onClick = { ContextCompat.startActivity(context, Showkase.getBrowserIntent(context), null) }
        )
        RageshakeDetectionView(
            state = state.rageshakeDetectionState,
            onOpenBugReport = onOpenBugReport,
            onDisableClicked = onDisableRageshake,
            onNoClicked = onDismissRageshake,
            onScreenshotTaken = onScreenshotTaken
        )
        CrashDetectionView(
            state = state.crashDetectionState,
            onOpenBugReport = onOpenBugReport,
            onPopupDismissed = onCrashDetectedDismissed
        )
    }
}
