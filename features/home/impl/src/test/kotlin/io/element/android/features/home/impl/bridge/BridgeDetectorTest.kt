/*
 * Copyright (c) 2025 Ravel.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BridgeDetectorTest {

    // --- Hero user ID detection ---

    @Test
    fun `detects WhatsApp from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@whatsappbot:matrix.org"))).isEqualTo(BridgeType.WHATSAPP)
        assertThat(BridgeDetector.detect(listOf("@whatsapp:example.com"))).isEqualTo(BridgeType.WHATSAPP)
    }

    @Test
    fun `detects Signal from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@signalbot:matrix.org"))).isEqualTo(BridgeType.SIGNAL)
        assertThat(BridgeDetector.detect(listOf("@signal:example.com"))).isEqualTo(BridgeType.SIGNAL)
    }

    @Test
    fun `detects Discord from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@discordbot:matrix.org"))).isEqualTo(BridgeType.DISCORD)
        assertThat(BridgeDetector.detect(listOf("@discord:example.com"))).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `detects Telegram from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@telegrambot:matrix.org"))).isEqualTo(BridgeType.TELEGRAM)
    }

    @Test
    fun `detects Meta from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@facebookbot:matrix.org"))).isEqualTo(BridgeType.META)
        assertThat(BridgeDetector.detect(listOf("@messengerbot:example.com"))).isEqualTo(BridgeType.META)
        assertThat(BridgeDetector.detect(listOf("@instagrambot:example.com"))).isEqualTo(BridgeType.META)
    }

    @Test
    fun `detects iMessage from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@imessagebot:matrix.org"))).isEqualTo(BridgeType.IMESSAGE)
        assertThat(BridgeDetector.detect(listOf("@imessage:example.com"))).isEqualTo(BridgeType.IMESSAGE)
    }

    @Test
    fun `detects Slack from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@slackbot:matrix.org"))).isEqualTo(BridgeType.SLACK)
    }

    @Test
    fun `detects Google Messages from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@gmessagesbot:matrix.org"))).isEqualTo(BridgeType.GOOGLE_MESSAGES)
        assertThat(BridgeDetector.detect(listOf("@gmessage:example.com"))).isEqualTo(BridgeType.GOOGLE_MESSAGES)
    }

    @Test
    fun `detects Google Chat from hero user ID`() {
        assertThat(BridgeDetector.detect(listOf("@gchatbot:matrix.org"))).isEqualTo(BridgeType.GOOGLE_CHAT)
        assertThat(BridgeDetector.detect(listOf("@googlechatbot:example.com"))).isEqualTo(BridgeType.GOOGLE_CHAT)
    }

    @Test
    fun `detects generic bridge from unknown bot user ID`() {
        assertThat(BridgeDetector.detect(listOf("@someunknownbot:matrix.org"))).isEqualTo(BridgeType.GENERIC)
    }

    @Test
    fun `returns null for regular Matrix user`() {
        assertThat(BridgeDetector.detect(listOf("@alice:matrix.org"))).isNull()
        assertThat(BridgeDetector.detect(listOf("@bob:example.com"))).isNull()
    }

    @Test
    fun `returns null for empty user list`() {
        assertThat(BridgeDetector.detect(emptyList())).isNull()
    }

    @Test
    fun `picks first bridge match from multiple heroes`() {
        // Real users mixed with a bridge bot — bot should be detected
        val heroes = listOf("@alice:matrix.org", "@discordbot:matrix.org", "@bob:matrix.org")
        assertThat(BridgeDetector.detect(heroes)).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `detection is case insensitive`() {
        assertThat(BridgeDetector.detect(listOf("@WhatsAppBot:matrix.org"))).isEqualTo(BridgeType.WHATSAPP)
        assertThat(BridgeDetector.detect(listOf("@DISCORDBOT:matrix.org"))).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `ignores homeserver domain in user ID`() {
        // Same local part, different homeservers — should all detect the same bridge
        assertThat(BridgeDetector.detect(listOf("@discordbot:server1.com"))).isEqualTo(BridgeType.DISCORD)
        assertThat(BridgeDetector.detect(listOf("@discordbot:server2.org"))).isEqualTo(BridgeType.DISCORD)
        assertThat(BridgeDetector.detect(listOf("@discordbot:myhomeserver.selfhosted.net"))).isEqualTo(BridgeType.DISCORD)
    }

    // --- Canonical alias fallback detection ---

    @Test
    fun `detects Discord from canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#discord_123456:matrix.org")).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `detects Telegram from canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#telegram_groupname:matrix.org")).isEqualTo(BridgeType.TELEGRAM)
    }

    @Test
    fun `detects WhatsApp from canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#whatsapp_15551234567:example.com")).isEqualTo(BridgeType.WHATSAPP)
    }

    @Test
    fun `detects Signal from canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#signal_group_abc123:example.com")).isEqualTo(BridgeType.SIGNAL)
    }

    @Test
    fun `detects Google Messages from canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#gmessages_15551234567:example.com")).isEqualTo(BridgeType.GOOGLE_MESSAGES)
    }

    @Test
    fun `returns null for non-bridge canonical alias`() {
        assertThat(BridgeDetector.detect(emptyList(), "#general:matrix.org")).isNull()
        assertThat(BridgeDetector.detect(emptyList(), "#random-room:example.com")).isNull()
    }

    @Test
    fun `hero detection takes priority over alias`() {
        // If heroes has a bot, use that — don't fall through to alias
        val result = BridgeDetector.detect(listOf("@signalbot:matrix.org"), "#discord_123:matrix.org")
        assertThat(result).isEqualTo(BridgeType.SIGNAL)
    }

    @Test
    fun `returns null for null alias with empty heroes`() {
        assertThat(BridgeDetector.detect(emptyList(), null)).isNull()
    }
}
