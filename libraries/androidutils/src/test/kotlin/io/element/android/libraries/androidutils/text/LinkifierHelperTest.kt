/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.text

import android.telephony.TelephonyManager
import android.text.style.URLSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import androidx.core.text.inSpans
import androidx.core.text.toSpannable
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Rule
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.newInstanceOf

class LinkifierHelperTest : RobolectricTest() {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `linkification finds URL`() {
        val text = "A url https://matrix.org"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://matrix.org")
    }

    @Test
    fun `linkification finds ipv6 URL`() {
        val text = "A url http://[2001:db8::1]:8008/path"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("http://[2001:db8::1]:8008/path")
    }

    @Test
    @Config(sdk = [30])
    fun `linkification of an ipv6 URL does not leave a phone number span`() {
        shadowOf(newInstanceOf(TelephonyManager::class.java)).setSimCountryIso("DE")
        val text = "A url http://[2001:db8::1]:8008/path"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("http://[2001:db8::1]:8008/path")
    }

    @Test
    fun `linkification of an ipv6 URL trims trailing punctuation`() {
        val text = "A url http://[::1]/x."
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("http://[::1]/x")
    }

    @Test
    fun `linkification finds an ipv6 URL of any scheme`() {
        val text = "A url ssh://[2001:db8::1]/path"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("ssh://[2001:db8::1]/path")
    }

    @Test
    fun `linkification ignores a bracketed authority which is not an address`() {
        val text = "A url http://[not-an-address]/path"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans).isEmpty()
    }

    @Test
    fun `linkification ignores bracketed text which is not a URL`() {
        val text = "An array values[0]:1 and a bare [::1]"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans).isEmpty()
    }

    @Test
    fun `linkification finds partial URL`() {
        val text = "A partial url matrix.org/test"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("http://matrix.org/test")
    }

    @Test
    fun `linkification finds domain`() {
        val text = "A domain matrix.org"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("http://matrix.org")
    }

    @Test
    fun `linkification finds email`() {
        val text = "An email address john@doe.com"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("mailto:john@doe.com")
    }

    @Test
    @Config(sdk = [30])
    fun `linkification finds phone`() {
        val text = "Test phone number +34950123456"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("tel:+34950123456")
    }

    @Test
    @Config(sdk = [30])
    fun `linkification finds phone in Germany`() {
        // For some reason the linkification of phone numbers in Germany is very lenient and any number will fit here
        val telephonyManager = shadowOf(newInstanceOf(TelephonyManager::class.java))
        telephonyManager.setSimCountryIso("DE")

        val text = "Test phone number 1234"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("tel:1234")
    }

    @Test
    fun `linkification handles trailing dot`() {
        val text = "A url https://matrix.org."
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://matrix.org")
    }

    @Test
    fun `linkification handles trailing punctuation`() {
        val text = "A url https://matrix.org!?; Check it out!"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://matrix.org")
    }

    @Test
    fun `linkification handles parenthesis surrounding URL`() {
        val text = "A url (this one (https://github.com/element-hq/element-android/issues/1234))"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android/issues/1234")
    }

    @Test
    fun `linkification handles parenthesis in URL`() {
        val text = "A url: (https://github.com/element-hq/element-android/READ(ME))"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android/READ(ME)")
    }

    @Test
    fun `linkification handles mismatched opening parenthesis in URL`() {
        val text = "A url: (https://github.com/element-hq/element-android/READ((((((ME))"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android/READ((((((ME))")
    }

    @Test
    fun `linkification handles mismatched closing parenthesis in URL`() {
        val text = "A url: (https://github.com/element-hq/element-android/READ(ME)))))"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android/READ(ME)")
    }

    @Test
    fun `linkification handles trailing question marks`() {
        val text = "A url: https://github.com/element-hq/element-android?"
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android")
    }

    @Test
    fun `linkification doesn't modify existing URLSpan`() {
        val text = buildSpannedString {
            append("A url: ")
            inSpans(URLSpan("https://github.com/element-hq/element-android?")) {
                append("here")
            }
        }
        val result = LinkifyHelper.linkify(text)
        val urlSpans = result.toSpannable().getSpans<URLSpan>()
        assertThat(urlSpans.size).isEqualTo(1)
        assertThat(urlSpans.first().url).isEqualTo("https://github.com/element-hq/element-android?")
    }
}
