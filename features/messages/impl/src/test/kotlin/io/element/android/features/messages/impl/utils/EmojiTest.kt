/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.utils

import org.junit.Assert
import org.junit.Test

class EmojiTest {
    @Test
    fun validEmojis() {
        // Simple single/multiple single-codepoint emojis per string
        Assert.assertTrue("👍".containsOnlyEmojis())
        Assert.assertTrue("😀".containsOnlyEmojis())
        Assert.assertTrue("🙂🙁".containsOnlyEmojis())
        Assert.assertTrue("👁❤️🍝".containsOnlyEmojis()) // 👁 is a pictographic
        Assert.assertTrue("👨‍👩‍👦1️⃣🚀👳🏾‍♂️🪩".containsOnlyEmojis())
        Assert.assertTrue("🌍🌎🌏".containsOnlyEmojis())

        // Awkward multi-codepoint graphemes
        Assert.assertTrue("🧑‍🧑‍🧒‍🧒".containsOnlyEmojis())
        Assert.assertTrue("🏴‍☠".containsOnlyEmojis())
        Assert.assertTrue("👩🏿‍🔧".containsOnlyEmojis())

        Assert.assertFalse("".containsOnlyEmojis())
        Assert.assertFalse(" ".containsOnlyEmojis())
        Assert.assertFalse("🙂 🙁".containsOnlyEmojis())
        Assert.assertFalse(" 🙂 🙁 ".containsOnlyEmojis())
        Assert.assertFalse("Hello".containsOnlyEmojis())
        Assert.assertFalse("Hello 👋".containsOnlyEmojis())
    }
}
