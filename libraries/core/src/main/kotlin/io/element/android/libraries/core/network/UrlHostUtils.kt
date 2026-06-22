/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.network

import java.net.URI

fun String.isLocalNetworkHost(): Boolean {
    val host = try {
        val uri = URI(if (contains("://")) this else "https://$this")
        uri.host ?: return false
    } catch (e: Exception) {
        return false
    }

    if (host.equals("localhost", ignoreCase = true)) return true

    // Check if host is an IP address and in private/local ranges
    return host in IPV4_LOCALHOST_ADDRESSES ||
        host.startsWith("127.") ||
        host.startsWith("10.") ||
        host.startsWith("192.168.") ||
        host.startsWith("169.254.") ||
        host.startsWith("0.") ||
        PRIVATE_172_RANGES.any { host.startsWith(it) } ||
        host == "::1" ||
        host.startsWith("fe80:")
}

private val PRIVATE_172_RANGES = (16..31).map { "172.$it." }

private val IPV4_LOCALHOST_ADDRESSES = setOf("127.0.0.1", "127.0.0.0")
