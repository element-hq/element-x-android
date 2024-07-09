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

package io.element.android.features.call.impl.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class CallScreenStateProvider : PreviewParameterProvider<CallScreenState> {
    override val values: Sequence<CallScreenState>
        get() = sequenceOf(
            aCallScreenState(),
            aCallScreenState(urlState = AsyncData.Loading()),
            aCallScreenState(urlState = AsyncData.Failure(Exception("An error occurred"))),
        )
}

internal fun aCallScreenState(
    urlState: AsyncData<String> = AsyncData.Success("https://call.element.io/some-actual-call?with=parameters"),
    userAgent: String = "",
    isInWidgetMode: Boolean = false,
    eventSink: (CallScreenEvents) -> Unit = {},
): CallScreenState {
    return CallScreenState(
        urlState = urlState,
        userAgent = userAgent,
        isInWidgetMode = isInWidgetMode,
        eventSink = eventSink,
    )
}
