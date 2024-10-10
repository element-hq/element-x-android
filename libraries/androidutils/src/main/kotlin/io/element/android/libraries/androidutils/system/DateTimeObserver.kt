/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import javax.inject.Inject

class DateTimeObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateTimeReceiver = object : BroadcastReceiver() {
        private var lastTime = Instant.now()

        override fun onReceive(context: Context, intent: Intent) {
            val newDate = Instant.now()
            when (intent.action) {
                Intent.ACTION_TIMEZONE_CHANGED -> _changes.tryEmit(Event.TimeZoneChanged)
                Intent.ACTION_DATE_CHANGED -> _changes.tryEmit(Event.DateChanged(lastTime, newDate))
            }
            lastTime = newDate
        }
    }

    private val _changes = MutableSharedFlow<Event>(extraBufferCapacity = 10)
    val changes: Flow<Event> = _changes

    init {
        context.registerReceiver(dateTimeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        })
    }

    sealed interface Event {
        data object TimeZoneChanged : Event
        data class DateChanged(val previous: Instant, val new: Instant) : Event
    }
}
