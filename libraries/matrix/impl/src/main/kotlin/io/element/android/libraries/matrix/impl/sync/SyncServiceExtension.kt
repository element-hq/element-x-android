/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

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
        state(listener)
    }.buffer(Channel.UNLIMITED)
