/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.LastLocation
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import org.matrix.rustcomponents.sdk.LiveLocationShareUpdate
import org.matrix.rustcomponents.sdk.LiveLocationsListener
import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.LiveLocationShare as RustLiveLocationShare

fun RoomInterface.liveLocationSharesFlow(): Flow<List<LiveLocationShare>> {
    fun MutableList<LiveLocationShare>.applyUpdate(update: LiveLocationShareUpdate) {
        when (update) {
            is LiveLocationShareUpdate.Append -> addAll(update.values.map { it.into() })
            is LiveLocationShareUpdate.Clear -> clear()
            is LiveLocationShareUpdate.Insert -> add(update.index.toInt(), update.value.into())
            is LiveLocationShareUpdate.PopBack -> if (isNotEmpty()) removeAt(lastIndex)
            is LiveLocationShareUpdate.PopFront -> if (isNotEmpty()) removeAt(0)
            is LiveLocationShareUpdate.PushBack -> add(update.value.into())
            is LiveLocationShareUpdate.PushFront -> add(0, update.value.into())
            is LiveLocationShareUpdate.Remove -> removeAt(update.index.toInt())
            is LiveLocationShareUpdate.Reset -> {
                clear()
                addAll(update.values.map { it.into() })
            }
            is LiveLocationShareUpdate.Set -> set(update.index.toInt(), update.value.into())
            is LiveLocationShareUpdate.Truncate -> subList(update.length.toInt(), size).clear()
        }
    }
    return callbackFlow {
        val liveLocationShares = liveLocationsObserver()
        val shares: MutableList<LiveLocationShare> = ArrayList()
        val taskHandle = liveLocationShares.subscribe(object : LiveLocationsListener {
            override fun onUpdate(updates: List<LiveLocationShareUpdate>) {
                for (update in updates) {
                    shares.applyUpdate(update)
                }
                trySend(shares)
            }
        })
        awaitClose {
            taskHandle.cancelAndDestroy()
            liveLocationShares.destroy()
        }
    }.buffer(Channel.UNLIMITED)
}

private fun RustLiveLocationShare.into(): LiveLocationShare {
    return LiveLocationShare(
        userId = UserId(userId),
        lastLocation = lastLocation?.let {
            LastLocation(
                geoUri = it.location.geoUri,
                timestamp = it.ts.toLong(),
                assetType = it.location.asset.into(),
            )
        },
        startTimestamp = startTs.toLong(),
        endTimestamp = (startTs + timeout).toLong()
    )
}
