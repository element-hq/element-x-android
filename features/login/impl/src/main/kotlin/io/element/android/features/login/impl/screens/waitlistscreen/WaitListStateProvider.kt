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

package io.element.android.features.login.impl.screens.waitlistscreen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.SessionId

open class WaitListStateProvider : PreviewParameterProvider<WaitListState> {
    override val values: Sequence<WaitListState>
        get() = sequenceOf(
            aWaitListState(loginAction = Async.Uninitialized),
            aWaitListState(loginAction = Async.Loading()),
            aWaitListState(loginAction = Async.Failure(Throwable())),
            aWaitListState(loginAction = Async.Failure(Throwable(message = "IO_ELEMENT_X_WAIT_LIST"))),
            // Add other state here
        )
}

fun aWaitListState(
    appName: String = "Element",
    serverName: String = "server.org",
    loginAction: Async<SessionId> = Async.Uninitialized,
) = WaitListState(
    appName = appName,
    serverName = serverName,
    loginAction = loginAction,
    eventSink = {}
)
