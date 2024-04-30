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

package io.element.android.features.messages.impl.timeline.focus

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.FocusRequestState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.errors.FocusEventException

open class FocusRequestStateProvider : PreviewParameterProvider<FocusRequestState> {
    override val values: Sequence<FocusRequestState>
        get() = sequenceOf(
            FocusRequestState.Fetching,
            FocusRequestState.Failure(
                FocusEventException.EventNotFound(
                    eventId = EventId("\$anEventId"),
                )
            ),
            FocusRequestState.Failure(
                FocusEventException.InvalidEventId(
                    eventId = "invalid",
                    err = "An error"
                )
            ),
            FocusRequestState.Failure(
                FocusEventException.Other(
                    msg = "An error"
                )
            ),
        )
}
