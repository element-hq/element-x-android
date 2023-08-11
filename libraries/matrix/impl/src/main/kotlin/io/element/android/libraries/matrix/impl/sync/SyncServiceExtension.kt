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

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import org.matrix.rustcomponents.sdk.SyncServiceInterface
import org.matrix.rustcomponents.sdk.SyncServiceState
import org.matrix.rustcomponents.sdk.SyncServiceStateObserver

fun SyncServiceInterface.stateFlow(): Flow<SyncServiceState> =
    mxCallbackFlow {
        val listener = object : SyncServiceStateObserver {
            override fun onUpdate(state: SyncServiceState) {
                trySendBlocking(state)
            }
        }
        tryOrNull {
            state(listener)
        }
    }.buffer(Channel.UNLIMITED)
