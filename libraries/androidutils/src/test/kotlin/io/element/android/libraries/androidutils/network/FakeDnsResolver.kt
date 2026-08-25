/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.network

import io.element.android.libraries.core.data.tryOrNull
import java.net.InetAddress
import java.net.UnknownHostException

class FakeDnsResolver(
    private val results: Map<String, List<InetAddress>> = emptyMap(),
    private val throwOnUnknown: Boolean = true,
) : DnsResolver {
    override suspend fun resolve(host: String): List<InetAddress> {
        // Mimic production DefaultDnsResolver: InetAddress resolves IP literals synchronously
        // (no DNS lookup), so tests can pass literals through without registering them in [results].
        if (looksLikeIpLiteral(host)) {
            tryOrNull { InetAddress.getAllByName(host).toList() }?.let { return it }
        }
        return results[host]
            ?: if (throwOnUnknown) throw UnknownHostException(host) else emptyList()
    }

    private fun looksLikeIpLiteral(host: String): Boolean {
        if (':' in host) return true
        val parts = host.split('.')
        return parts.size == 4 && parts.all { it.toIntOrNull() != null }
    }
}
