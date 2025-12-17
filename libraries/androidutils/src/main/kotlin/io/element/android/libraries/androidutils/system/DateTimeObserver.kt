/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.system.DateTimeObserver.Event
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant

interface DateTimeObserver {
    val changes: Flow<Event>

    sealed interface Event {
        data object TimeZoneChanged : Event
        data class DateChanged(val previous: Instant, val new: Instant) : Event
    }
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultDateTimeObserver(
    @ApplicationContext context: Context
) : DateTimeObserver {
    private val dateTimeReceiver = object : BroadcastReceiver() {
        private var lastTime = Instant.now()

        override fun onReceive(context: Context, intent: Intent) {
            val newDate = Instant.now()
            when (intent.action) {
                Intent.ACTION_TIMEZONE_CHANGED -> changes.tryEmit(Event.TimeZoneChanged)
                Intent.ACTION_DATE_CHANGED -> changes.tryEmit(Event.DateChanged(lastTime, newDate))
                Intent.ACTION_TIME_CHANGED -> changes.tryEmit(Event.DateChanged(lastTime, newDate))
            }
            lastTime = newDate
        }
    }

    override val changes = MutableSharedFlow<Event>(extraBufferCapacity = 10)

    init {
        context.registerReceiver(dateTimeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        })
    }
}
