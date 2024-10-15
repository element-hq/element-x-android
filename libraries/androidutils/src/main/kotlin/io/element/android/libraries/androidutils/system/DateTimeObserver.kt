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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.DateTimeObserver.Event
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import javax.inject.Inject

interface DateTimeObserver {
    val changes: Flow<Event>

    sealed interface Event {
        data object TimeZoneChanged : Event
        data class DateChanged(val previous: Instant, val new: Instant) : Event
    }
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultDateTimeObserver @Inject constructor(
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
