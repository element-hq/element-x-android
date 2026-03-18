/*
 * Copyright (c) 2025 Ravel.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

object BridgeDetector {
    /**
     * Returns the first detected bridge type from a list of user IDs.
     * Looks at the local part (before the colon) of each user ID.
     */
    fun detect(userIds: List<String>): BridgeType? {
        for (userId in userIds) {
            val localPart = userId.removePrefix("@").substringBefore(":").lowercase()
            val type = matchLocalPart(localPart)
            if (type != null) return type
        }
        return null
    }

    private fun matchLocalPart(localPart: String): BridgeType? = when {
        localPart.contains("whatsapp") -> BridgeType.WHATSAPP
        localPart.contains("signal") -> BridgeType.SIGNAL
        localPart.contains("discord") -> BridgeType.DISCORD
        localPart.contains("telegram") -> BridgeType.TELEGRAM
        localPart.contains("facebook") || localPart.contains("messenger") || localPart.contains("instagram") || localPart.contains("meta") -> BridgeType.META
        localPart.contains("imessage") || localPart.contains("apple") -> BridgeType.IMESSAGE
        localPart.contains("slack") -> BridgeType.SLACK
        localPart.contains("gchat") || localPart.contains("googlechat") || localPart.contains("google") -> BridgeType.GOOGLE_CHAT
        localPart.endsWith("bot") -> BridgeType.GENERIC
        else -> null
    }
}
