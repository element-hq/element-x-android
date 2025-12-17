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
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.WarmUpRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.newInstanceOf

@RunWith(RobolectricTestRunner::class)
class LinkifierHelperTest {
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
}
