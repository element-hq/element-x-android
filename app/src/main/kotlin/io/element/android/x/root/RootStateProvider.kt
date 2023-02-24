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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.rageshake.crash.ui.aCrashDetectionState
import io.element.android.features.rageshake.detection.aRageshakeDetectionState

open class RootStateProvider : PreviewParameterProvider<RootState> {
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

fun aRootState() = RootState(
    isShowkaseButtonVisible = false,
    rageshakeDetectionState = aRageshakeDetectionState(),
    crashDetectionState = aCrashDetectionState(),
    eventSink = {}
)
