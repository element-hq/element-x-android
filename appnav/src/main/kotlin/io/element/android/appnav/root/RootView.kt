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

package io.element.android.appnav.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.rageshake.api.crash.CrashDetectionEvents
import io.element.android.features.rageshake.api.crash.CrashDetectionView
import io.element.android.features.rageshake.api.detection.RageshakeDetectionEvents
import io.element.android.features.rageshake.api.detection.RageshakeDetectionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.services.apperror.impl.AppErrorView

@Composable
fun RootView(
    state: RootState,
    onOpenBugReport: () -> Unit,
    modifier: Modifier = Modifier,
    children: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        children()

        fun onOpenBugReport() {
            state.crashDetectionState.eventSink(CrashDetectionEvents.ResetAppHasCrashed)
            state.rageshakeDetectionState.eventSink(RageshakeDetectionEvents.Dismiss)
            onOpenBugReport.invoke()
        }

        RageshakeDetectionView(
            state = state.rageshakeDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
        CrashDetectionView(
            state = state.crashDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
        AppErrorView(
            state = state.errorState,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RootViewPreview(@PreviewParameter(RootStateProvider::class) rootState: RootState) = ElementPreview {
    RootView(
        state = rootState,
        onOpenBugReport = {},
    ) {
        Text("Children")
    }
}
