/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

object BridgeDetector {
    /**
     * Detects bridge type from hero user IDs (DM rooms) with fallback to canonical alias (group rooms).
     * mautrix bridge bots use consistent local part naming across all homeservers.
     * mautrix group room aliases default to: {bridgename}_{identifier}
     */
    fun detect(userIds: List<String>, canonicalAlias: String? = null): BridgeType? {
        // Primary: check hero user IDs (works for DMs where bot is a hero)
        for (userId in userIds) {
            val localPart = userId.removePrefix("@").substringBefore(":").lowercase()
            val type = matchLocalPart(localPart)
            if (type != null) return type
        }
        // Fallback: check canonical alias local part (works for group rooms)
        // Alias format: #discord_123456:server or #telegram_groupname:server
        if (canonicalAlias != null) {
            val aliasLocal = canonicalAlias.removePrefix("#").substringBefore(":").substringBefore("_").lowercase()
            return matchAliasPrefix(aliasLocal)
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
        localPart.contains("gmessage") || localPart.contains("rcs") -> BridgeType.GOOGLE_MESSAGES
        localPart.contains("gchat") || localPart.contains("googlechat") || localPart.contains("google") -> BridgeType.GOOGLE_CHAT
        localPart.endsWith("bot") -> BridgeType.GENERIC
        else -> null
    }

    private fun matchAliasPrefix(prefix: String): BridgeType? = when (prefix) {
        "whatsapp" -> BridgeType.WHATSAPP
        "signal" -> BridgeType.SIGNAL
        "discord" -> BridgeType.DISCORD
        "telegram" -> BridgeType.TELEGRAM
        "facebook", "messenger", "instagram", "meta" -> BridgeType.META
        "imessage", "apple" -> BridgeType.IMESSAGE
        "slack" -> BridgeType.SLACK
        "gmessages", "gmessage", "rcs" -> BridgeType.GOOGLE_MESSAGES
        "gchat", "googlechat" -> BridgeType.GOOGLE_CHAT
        else -> null
    }
}
