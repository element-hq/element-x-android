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

package io.element.android.features.messages.impl.forward

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

open class ForwardMessagesStateProvider : PreviewParameterProvider<ForwardMessagesState> {
    override val values: Sequence<ForwardMessagesState>
        get() = sequenceOf(
            aForwardMessagesState(),
            aForwardMessagesState(
                forwardAction = AsyncAction.Loading,
            ),
            aForwardMessagesState(
                forwardAction = AsyncAction.Success(
                    listOf(RoomId("!room2:domain")),
                )
            ),
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(Throwable("error")),
            ),
        )
}

fun aForwardMessagesState(
    forwardAction: AsyncAction<List<RoomId>> = AsyncAction.Uninitialized,
) = ForwardMessagesState(
    forwardAction = forwardAction,
    eventSink = {}
)
