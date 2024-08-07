/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter

class ResetKeyRootPresenter : Presenter<ResetKeyRootState> {
    @Composable
    override fun present(): ResetKeyRootState {
        var displayConfirmDialog by remember { mutableStateOf(false) }

        fun handleEvent(event: ResetKeyRootEvent) {
            displayConfirmDialog = when (event) {
                ResetKeyRootEvent.Continue -> true
                ResetKeyRootEvent.DismissDialog -> false
            }
        }

        return ResetKeyRootState(
            displayConfirmationDialog = displayConfirmDialog,
            eventSink = ::handleEvent
        )
    }
}
