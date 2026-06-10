/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.service

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext

interface ServiceBinder {
    fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean
    fun unbindService(conn: ServiceConnection)
}

@ContributesBinding(AppScope::class)
class DefaultServiceBinder(
    @ApplicationContext private val context: Context,
) : ServiceBinder {
    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        return context.bindService(service, conn, flags)
    }

    override fun unbindService(conn: ServiceConnection) {
        context.unbindService(conn)
    }
}
