/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uniffi.matrix_sdk_ui.PaginationStatus

fun Flow<PaginationStatus>.map(): Flow<Timeline.PaginationStatus> = map { paginationStatus ->
    when (paginationStatus) {
        PaginationStatus.IDLE -> Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = true
        )
        PaginationStatus.PAGINATING -> Timeline.PaginationStatus(
            isPaginating = true,
            hasMoreToLoad = true
        )
        PaginationStatus.TIMELINE_END_REACHED -> Timeline.PaginationStatus(
            isPaginating = false,
            hasMoreToLoad = false
        )
    }
}

