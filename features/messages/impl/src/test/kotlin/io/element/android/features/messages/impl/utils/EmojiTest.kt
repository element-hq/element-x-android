/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
