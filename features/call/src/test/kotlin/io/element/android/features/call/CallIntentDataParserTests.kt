/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URLEncoder

@RunWith(RobolectricTestRunner::class)
class CallIntentDataParserTests {

    private val callIntentDataParser = CallIntentDataParser()

    @Test
    fun `a null data returns null`() {
        val url: String? = null
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `empty data returns null`() {
        val url = ""
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `invalid data returns null`() {
        val url = "!"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `data with no scheme returns null`() {
        val url = "test"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `Element Call urls will be returned as is`() {
        val httpBaseUrl = "http://call.element.io"
        val httpCallUrl = "http://call.element.io/some-actual-call?with=parameters"
        val httpsBaseUrl = "https://call.element.io"
        val httpsCallUrl = "https://call.element.io/some-actual-call?with=parameters"
        assertThat(callIntentDataParser.parse(httpBaseUrl)).isEqualTo(httpBaseUrl)
        assertThat(callIntentDataParser.parse(httpCallUrl)).isEqualTo(httpCallUrl)
        assertThat(callIntentDataParser.parse(httpsBaseUrl)).isEqualTo(httpsBaseUrl)
        assertThat(callIntentDataParser.parse(httpsCallUrl)).isEqualTo(httpsCallUrl)
    }

    @Test
    fun `HTTP and HTTPS urls that don't come from EC return null`() {
        val httpBaseUrl = "http://app.element.io"
        val httpsBaseUrl = "https://app.element.io"
        val httpInvalidUrl = "http://"
        val httpsInvalidUrl = "http://"
        assertThat(callIntentDataParser.parse(httpBaseUrl)).isNull()
        assertThat(callIntentDataParser.parse(httpsBaseUrl)).isNull()
        assertThat(callIntentDataParser.parse(httpInvalidUrl)).isNull()
        assertThat(callIntentDataParser.parse(httpsInvalidUrl)).isNull()
    }

    @Test
    fun `element scheme with call host and url param gets url extracted`() {
        val embeddedUrl = "http://call.element.io/some-actual-call?with=parameters"
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "element://call?url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isEqualTo(embeddedUrl)
    }

    @Test
    fun `element scheme 2 with url param gets url extracted`() {
        val embeddedUrl = "http://call.element.io/some-actual-call?with=parameters"
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "io.element.call:/?url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isEqualTo(embeddedUrl)
    }

    @Test
    fun `element scheme with call host and no url param returns null`() {
        val embeddedUrl = "http://call.element.io/some-actual-call?with=parameters"
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "element://call?no-url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element scheme 2 with no url returns null`() {
        val embeddedUrl = "http://call.element.io/some-actual-call?with=parameters"
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "io.element.call:/?no_url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element scheme with no call host returns null`() {
        val embeddedUrl = "http://call.element.io/some-actual-call?with=parameters"
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "element://no-call?url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element scheme with no data returns null`() {
        val url = "element://call?url="
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element scheme 2 with no data returns null`() {
        val url = "io.element.call:/?url="
        assertThat(callIntentDataParser.parse(url)).isNull()
    }
}
