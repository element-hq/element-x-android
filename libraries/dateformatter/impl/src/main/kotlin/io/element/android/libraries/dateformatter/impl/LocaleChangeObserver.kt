/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

fun interface LocaleChangeObserver {
    fun addListener(listener: LocaleChangeListener)
}

interface LocaleChangeListener {
    fun onLocaleChange()
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultLocaleChangeObserver @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocaleChangeObserver {
    init {
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                listeners.forEach(LocaleChangeListener::onLocaleChange)
            }
        })
    }

    private val listeners = mutableSetOf<LocaleChangeListener>()

    override fun addListener(listener: LocaleChangeListener) {
        listeners.add(listener)
    }

    private fun registerReceiver(receiver: BroadcastReceiver) {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_LOCALE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            filter.addAction(Intent.ACTION_APPLICATION_LOCALE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
    }
}
