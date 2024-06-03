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

package io.element.android.features.call.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.utils.CallIntentDataParser
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URLEncoder

@RunWith(RobolectricTestRunner::class)
class CallIntentDataParserTest {
    private val callIntentDataParser = CallIntentDataParser()

    @Test
    fun `a null data returns null`() {
        val url: String? = null
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `empty data returns null`() {
        doTest("", null)
    }

    @Test
    fun `invalid data returns null`() {
        doTest("!", null)
    }

    @Test
    fun `data with no scheme returns null`() {
        doTest("test", null)
    }

    @Test
    fun `Element Call http urls returns null`() {
        doTest("http://call.element.io", null)
        doTest("http://call.element.io/some-actual-call?with=parameters", null)
    }

    @Test
    fun `Element Call urls will be returned as is`() {
        doTest(
            url = "https://call.element.io",
            expectedResult = "https://call.element.io#?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with url param gets url extracted`() {
        doTest(
            url = VALID_CALL_URL_WITH_PARAM,
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `HTTP and HTTPS urls that don't come from EC return null`() {
        doTest("http://app.element.io", null)
        doTest("https://app.element.io", null, testEmbedded = false)
        doTest("http://", null)
        doTest("https://", null)
    }

    @Test
    fun `Element Call url with no url returns null`() {
        val embeddedUrl = VALID_CALL_URL_WITH_PARAM
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "io.element.call:/?no_url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element scheme with no call host returns null`() {
        val embeddedUrl = VALID_CALL_URL_WITH_PARAM
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
    fun `Element Call url with no data returns null`() {
        val url = "io.element.call:/?url="
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `element invalid scheme returns null`() {
        val embeddedUrl = VALID_CALL_URL_WITH_PARAM
        val encodedUrl = URLEncoder.encode(embeddedUrl, "utf-8")
        val url = "bad.scheme:/?url=$encodedUrl"
        assertThat(callIntentDataParser.parse(url)).isNull()
    }

    @Test
    fun `Element Call url with url extra param appPrompt gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM&appPrompt=true",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with url extra param in fragment appPrompt gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?appPrompt=true",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?appPrompt=false&confineToRoom=true"
        )
    }

    @Test
    fun `Element Call url with url extra param in fragment appPrompt and other gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?appPrompt=true&otherParam=maybe",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?appPrompt=false&otherParam=maybe&confineToRoom=true"
        )
    }

    @Test
    fun `Element Call url with url extra param confineToRoom gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM&confineToRoom=false",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with url extra param in fragment confineToRoom gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?confineToRoom=false",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?confineToRoom=true&appPrompt=false"
        )
    }

    @Test
    fun `Element Call url with url extra param in fragment confineToRoom and more gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?confineToRoom=false&otherParam=maybe",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?confineToRoom=true&otherParam=maybe&appPrompt=false"
        )
    }

    @Test
    fun `Element Call url with url fragment gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#fragment",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#fragment?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with url fragment with params gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#fragment?otherParam=maybe",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#fragment?otherParam=maybe&$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with url fragment with other params gets url extracted`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?otherParam=maybe",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?otherParam=maybe&$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with empty fragment`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?$EXTRA_PARAMS"
        )
    }

    @Test
    fun `Element Call url with empty fragment query`() {
        doTest(
            url = "$VALID_CALL_URL_WITH_PARAM#?",
            expectedResult = "$VALID_CALL_URL_WITH_PARAM#?$EXTRA_PARAMS"
        )
    }

    private fun doTest(url: String, expectedResult: String?, testEmbedded: Boolean = true) {
        // Test direct parsing
        assertThat(callIntentDataParser.parse(url)).isEqualTo(expectedResult)

        if (testEmbedded) {
            // Test embedded url, scheme 1
            val encodedUrl = URLEncoder.encode(url, "utf-8")
            val urlScheme1 = "element://call?url=$encodedUrl"
            assertThat(callIntentDataParser.parse(urlScheme1)).isEqualTo(expectedResult)

            // Test embedded url, scheme 2
            val urlScheme2 = "io.element.call:/?url=$encodedUrl"
            assertThat(callIntentDataParser.parse(urlScheme2)).isEqualTo(expectedResult)
        }
    }

    companion object {
        const val VALID_CALL_URL_WITH_PARAM = "https://call.element.io/some-actual-call?with=parameters"
        const val EXTRA_PARAMS = "appPrompt=false&confineToRoom=true"
    }
}
