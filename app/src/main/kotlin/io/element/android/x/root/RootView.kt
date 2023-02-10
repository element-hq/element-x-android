/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.x.root

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.rageshake.crash.ui.CrashDetectionEvents
import io.element.android.features.rageshake.crash.ui.CrashDetectionView
import io.element.android.features.rageshake.crash.ui.aCrashDetectionState
import io.element.android.features.rageshake.detection.RageshakeDetectionEvents
import io.element.android.features.rageshake.detection.RageshakeDetectionView
import io.element.android.features.rageshake.detection.aRageshakeDetectionState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.tests.uitests.openShowkase
import io.element.android.x.component.ShowkaseButton

@Composable
fun RootView(
    state: RootState,
    modifier: Modifier = Modifier,
    onOpenBugReport: () -> Unit = {},
    children: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        children()
        val eventSink = state.eventSink
        val context = LocalContext.current

        fun onOpenBugReport() {
            state.crashDetectionState.eventSink(CrashDetectionEvents.ResetAppHasCrashed)
            state.rageshakeDetectionState.eventSink(RageshakeDetectionEvents.Dismiss)
            onOpenBugReport.invoke()
        }

        ShowkaseButton(
            isVisible = state.isShowkaseButtonVisible,
            onCloseClicked = { eventSink(RootEvents.HideShowkaseButton) },
            onClick = { openShowkase(context as Activity) }
        )
        RageshakeDetectionView(
            state = state.rageshakeDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
        CrashDetectionView(
            state = state.crashDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
    }
}

open class RootStatePreviewParameter : PreviewParameterProvider<RootState> {
    override val values: Sequence<RootState>
        get() = sequenceOf(
            aRootState().copy(
                isShowkaseButtonVisible = true,
                rageshakeDetectionState = aRageshakeDetectionState().copy(showDialog = false),
                crashDetectionState = aCrashDetectionState().copy(crashDetected = true),
            ),
            aRootState().copy(
                isShowkaseButtonVisible = true,
                rageshakeDetectionState = aRageshakeDetectionState().copy(showDialog = true),
                crashDetectionState = aCrashDetectionState().copy(crashDetected = false),
            )
        )
}

@Preview
@Composable
fun RootLightPreview(@PreviewParameter(RootStatePreviewParameter::class) rootState: RootState) = ElementPreviewLight { ContentToPreview(rootState) }

@Preview
@Composable
fun RootDarkPreview(@PreviewParameter(RootStatePreviewParameter::class) rootState: RootState) = ElementPreviewDark { ContentToPreview(rootState) }

@Composable
private fun ContentToPreview(rootState: RootState) {
    RootView(rootState) {
        Text("Children")
    }
}
