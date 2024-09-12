/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntriesListener
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate
import timber.log.Timber

internal fun RoomDirectorySearch.resultsFlow(): Flow<List<RoomDirectorySearchEntryUpdate>> =
    callbackFlow {
        val listener = object : RoomDirectorySearchEntriesListener {
            override fun onUpdate(roomEntriesUpdate: List<RoomDirectorySearchEntryUpdate>) {
                trySendBlocking(roomEntriesUpdate)
            }
        }
        val result = results(listener)
        awaitClose {
            result.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "timelineDiffFlow() failed")
    }.buffer(Channel.UNLIMITED)
