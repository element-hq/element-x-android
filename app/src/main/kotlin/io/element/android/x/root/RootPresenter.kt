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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.rageshake.crash.ui.CrashDetectionPresenter
import io.element.android.features.rageshake.detection.RageshakeDetectionPresenter
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class RootPresenter @Inject constructor(
    private val crashDetectionPresenter: CrashDetectionPresenter,
    private val rageshakeDetectionPresenter: RageshakeDetectionPresenter,
) : Presenter<RootState> {

    @Composable
    override fun present(): RootState {
        val isShowkaseButtonVisible = rememberSaveable {
            mutableStateOf(true)
        }
        val rageshakeDetectionState = rageshakeDetectionPresenter.present()
        val crashDetectionState = crashDetectionPresenter.present()

        fun handleEvent(event: RootEvents) {
            when (event) {
                RootEvents.HideShowkaseButton -> isShowkaseButtonVisible.value = false
            }
        }

        return RootState(
            isShowkaseButtonVisible = isShowkaseButtonVisible.value,
            rageshakeDetectionState = rageshakeDetectionState,
            crashDetectionState = crashDetectionState,
            eventSink = ::handleEvent
        )
    }
}
