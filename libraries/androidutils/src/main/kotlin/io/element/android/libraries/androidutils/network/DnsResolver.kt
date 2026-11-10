/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

interface DnsResolver {
    suspend fun resolve(host: String): List<InetAddress>
}

@ContributesBinding(AppScope::class)
class DefaultDnsResolver(
    private val coroutineDispatchers: CoroutineDispatchers,
) : DnsResolver {
    override suspend fun resolve(host: String): List<InetAddress> = withContext(coroutineDispatchers.io) {
        InetAddress.getAllByName(host).toList()
    }
}
