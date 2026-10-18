/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.data.tryOrNull
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI

sealed interface LocalNetworkClassification {
    data object PublicIp : LocalNetworkClassification
    data object LocalIp : LocalNetworkClassification
    data object Unresolvable : LocalNetworkClassification
}

interface LocalNetworkAddressClassifier {
    /**
     * Classify [url] as pointing to a local network, a public host, or unresolvable.
     */
    suspend fun classify(url: String): LocalNetworkClassification
}

@ContributesBinding(AppScope::class)
class DefaultLocalNetworkAddressClassifier(
    private val dnsResolver: DnsResolver,
) : LocalNetworkAddressClassifier {
    override suspend fun classify(url: String): LocalNetworkClassification {
        val host = extractHost(url) ?: return LocalNetworkClassification.Unresolvable
        // `.local` domains are always link-local by definition
        if (host.endsWith(".local", ignoreCase = true)) return LocalNetworkClassification.LocalIp

        val resolved = tryOrNull { dnsResolver.resolve(host) }

        if (resolved.isNullOrEmpty()) return LocalNetworkClassification.Unresolvable

        return if (resolved.any { it.isLocalRange() }) {
            LocalNetworkClassification.LocalIp
        } else {
            LocalNetworkClassification.PublicIp
        }
    }

    private fun extractHost(url: String): String? {
        return tryOrNull {
            val uri = URI(url)
            uri.host?.takeIf { it.isNotBlank() }
        }
    }

    private fun InetAddress.isLocalRange(): Boolean {
        if (isLoopbackAddress || isLinkLocalAddress || isSiteLocalAddress) return true
        // Cases not covered by jvm
        // 1. IPV4 100.64.0.0/10
        if (this is Inet4Address) {
            val bytes = address
            val b0 = bytes[0].toInt() and 0xff
            val b1 = bytes[1].toInt() and 0xff
            if (b0 == 100 && b1 in 64..127) return true
        }
        // 2. IPV6 fc00::/7
        if (this is Inet6Address) {
            val firstByte = address[0].toInt() and 0xff
            if (firstByte and 0xfe == 0xfc) return true
        }
        return false
    }
}
