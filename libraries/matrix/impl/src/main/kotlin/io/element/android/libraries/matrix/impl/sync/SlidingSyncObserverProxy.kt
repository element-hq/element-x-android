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

package io.element.android.libraries.matrix.impl.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.SlidingSyncObserver
import org.matrix.rustcomponents.sdk.UpdateSummary

// Sounds like a reasonable buffer size before it suspends emitting new items.
private const val BUFFER_SIZE = 64

class SlidingSyncObserverProxy(
    private val coroutineScope: CoroutineScope,
) : SlidingSyncObserver {

    private val updateSummaryMutableFlow =
        MutableSharedFlow<UpdateSummary>(extraBufferCapacity = BUFFER_SIZE)
    val updateSummaryFlow: SharedFlow<UpdateSummary> = updateSummaryMutableFlow.asSharedFlow()

    override fun didReceiveSyncUpdate(summary: UpdateSummary) {
        coroutineScope.launch {
            updateSummaryMutableFlow.emit(summary)
        }
    }
}
