/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

interface DnsResolver {
    suspend fun resolve(host: String): List<InetAddress>
}

@ContributesBinding(AppScope::class)
class DefaultDnsResolver : DnsResolver {
    override suspend fun resolve(host: String): List<InetAddress> = withContext(Dispatchers.IO) {
        InetAddress.getAllByName(host).toList()
    }
}
